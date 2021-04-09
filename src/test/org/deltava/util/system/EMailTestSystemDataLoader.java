// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.system;

import java.io.IOException;
import java.util.Map;

public class EMailTestSystemDataLoader extends XMLSystemDataLoader {

	@Override
    public Map<String, Object> load() throws IOException {
		Map<String, Object> props = super.load();
		props.put("smtp.server", "sirius.sce.net");
		return props;
	}
}