/*
* The MIT License
*
* Copyright (c) 2013 Edin Dazdarevic (edin.dazdarevic@gmail.com)
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*
* */

package org.deltava.util;

import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * A class that enables to get an IP range from CIDR specification. It supports both IPv4 and IPv6.
 */
public class CIDRBlock {
	
    private final String _cidr;

    private InetAddress _inetAddress;
    private InetAddress _startAddress;
    private InetAddress _endAddress;
    private final int _prefixLength;

    public CIDRBlock(String cidrAddr) throws UnknownHostException {
    	super();
        _cidr = cidrAddr;

        /* split CIDR to address and prefix part */
        int index = _cidr.indexOf('/');
        if (index > -1) {
            String addressPart = _cidr.substring(0, index);
            String networkPart = _cidr.substring(index + 1);

            _inetAddress = InetAddress.getByName(addressPart);
            _prefixLength = Integer.parseInt(networkPart);

            calculate();
        } else
            throw new IllegalArgumentException("not an valid CIDR format!");
    }

    private void calculate() throws UnknownHostException {
        
        int targetSize = _inetAddress.getAddress().length;
        ByteBuffer maskBuffer = ByteBuffer.allocate(targetSize);
        if (targetSize == 4)
            maskBuffer.putInt(-1);
        else
            maskBuffer.putLong(-1L).putLong(-1L);

        BigInteger mask = (new BigInteger(1, maskBuffer.array())).not().shiftRight(_prefixLength);
        ByteBuffer buffer = ByteBuffer.wrap(_inetAddress.getAddress());
        BigInteger ipVal = new BigInteger(1, buffer.array());

        BigInteger startIp = ipVal.and(mask);
        BigInteger endIp = startIp.add(mask.not());

        byte[] startIpArr = toBytes(startIp.toByteArray(), targetSize);
        byte[] endIpArr = toBytes(endIp.toByteArray(), targetSize);

        _startAddress = InetAddress.getByAddress(startIpArr);
        _endAddress = InetAddress.getByAddress(endIpArr);
    }

    private static byte[] toBytes(byte[] array, int targetSize) {
        int counter = 0;
        List<Byte> newArr = new ArrayList<Byte>();
        while (counter < targetSize && (array.length - 1 - counter >= 0)) {
            newArr.add(0, Byte.valueOf(array[array.length - 1 - counter]));
            counter++;
        }

        int size = newArr.size();
        for (int i = 0; i < (targetSize - size); i++)
            newArr.add(0, Byte.valueOf((byte)0));

        byte[] ret = new byte[newArr.size()];
        for (int i = 0; i < newArr.size(); i++)
            ret[i] = newArr.get(i).byteValue();

        return ret;
    }
    
    public boolean isIPv6() {
    	return (_startAddress instanceof Inet6Address);
    }
    
    public String getNetworkAddress() {
        return _startAddress.getHostAddress();
    }

    public String getBroadcastAddress() {
        return _endAddress.getHostAddress();
    }
    
    public int getPrefixLength() {
    	return _prefixLength;
    }

    public boolean isInRange(String ipAddress) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ipAddress);
        BigInteger start = new BigInteger(1, _startAddress.getAddress());
        BigInteger end = new BigInteger(1, _endAddress.getAddress());
        BigInteger target = new BigInteger(1, address.getAddress());

        int st = start.compareTo(target);
        int te = target.compareTo(end);

        return (st == -1 || st == 0) && (te == -1 || te == 0);
    }
}