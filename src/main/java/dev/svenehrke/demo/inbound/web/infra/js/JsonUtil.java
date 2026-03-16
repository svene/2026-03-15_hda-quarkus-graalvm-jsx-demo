package dev.svenehrke.demo.inbound.web.infra.js;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

public class JsonUtil {

	public static String toJson(Object obj)  {
		try (Jsonb jsonb = JsonbBuilder.create()) {
			return jsonb.toJson(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
