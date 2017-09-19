package org.deltava.acars;

import java.io.*;
import java.sql.*;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import org.gvagroup.tile.TileAddress;

public class WriteWorker implements Runnable, Comparable<WriteWorker> {

	private final Connection _c;
	private final PreparedStatement _ps;
	
	private final int _id;
	private final BlockingQueue<TileAddress> _work;
	private final SparseGlobalTile _in;
	private final SparseGlobalTile _out;
	private final Logger log;
	
	private final ProjectInfo _ppp;
	
	public WriteWorker(int id, ProjectInfo ppp, Connection c, BlockingQueue<TileAddress> work, SparseGlobalTile in, SparseGlobalTile out) {
		super();
		_id = id;
		_ppp = ppp;
		_c = c;
		_work = work;
		_in = in;
		_out = out;
		log = Logger.getLogger(WriteWorker.class.getPackage().getName() + "." + toString());
		try {
			_ps = _c.prepareStatement("REPLACE INTO acars.TRACKS (X, Y, Z, SIZE, IMG) VALUES (?, ?, ?, ?, ?)");
		} catch (SQLException se) {
			throw new RuntimeException(se);
		}
	}

	@Override
	public String toString() {
		return "WriteWorker-" + _id;
	}

	@Override
	public int compareTo(WriteWorker ww2) {
		return Integer.compare(_id, _id);
	}

	@Override
	public void run() {
		int cnt = 0;
		TileAddress addr = _work.poll();
		while (addr != null) {
			int px = addr.getPixelX() << 1; int py = addr.getPixelY() << 1;
			Collection<TileAddress> addrs = addr.getChildren();

			RawTile img = RawTile.getTile(_ppp.getMax());
			for (TileAddress ch : addrs) {
				TileData td = _in.getTile(ch, false);
				if (td == null)
					continue;

				// Write the image
				RawTile srcImg = td.getImage();
				writeImage(ch, srcImg);

				// Do the scaling
				int dX = (ch.getPixelX() - px) >> 1; int dY = (ch.getPixelY() - py) >> 1;
				for (int x = 0; x < 256; x += 2) {
					for (int y = 0; y < 256; y += 2) {
						int pxCnt = srcImg.getCount(x, y);
						pxCnt += srcImg.getCount(x, y + 1);
						pxCnt += srcImg.getCount(x + 1, y);
						pxCnt += srcImg.getCount(x + 1, y + 1);

						int pX = dX + (x >> 1); int pY = dY + (y >> 1);
						img.set(pX, pY, Math.min(pxCnt, _ppp.getMax()));
					}
				}
				
				_in.remove(ch);
				cnt++;
			}

			// Write the scaled image
			TileData td = new TileData(img);
			_out.put(addr, td);
			addr = _work.poll();
		}

		try {
			_ps.close();
			_c.close();
		} catch (Exception e) { 
			/* empty */
		} finally {
			log.info("Scaled " + cnt + " tiles");	
		}
	}

	private void writeImage(TileAddress addr, RawTile img) {
		
		// Convert the image
		boolean hasData = false;
		BufferedImage png = new BufferedImage(256, 256, BufferedImage.TYPE_4BYTE_ABGR);
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++) {
				int cnt = Math.min(img.getCount(x, y), _ppp.getMax());
				if (cnt >= _ppp.getMin()) {
					hasData = true;
					float value = (cnt *0.9f / _ppp.getMax()) + 0.105f;
					int v = Math.min(255, (int)(value*255));
					int rgb = (v << 16) + (v << 8) + v;
					png.setRGB(x, y, 0xFF000000 | rgb);
				}
			}
		}
		
		try {
			if (hasData) {
				try (ByteArrayOutputStream buf = new ByteArrayOutputStream(512)) {
					ImageIO.write(png, "png", buf);
					_ps.setInt(1, addr.getX());
					_ps.setInt(2, addr.getY());
					_ps.setInt(3, addr.getLevel());
					_ps.setInt(4, buf.size());
					_ps.setBytes(5, buf.toByteArray());
					_ps.executeUpdate();
				}
			}
		} catch (IOException | SQLException ie) {
			log.error("Error writing " + addr + " - " + ie.getMessage());
		} finally {
			png.flush();
		}
	}
}