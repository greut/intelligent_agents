package logist.config;

import java.awt.Color;
import java.io.File;
import java.util.Map.Entry;

import logist.LogistSettings;
import logist.LogistSettings.ColorKey;
import logist.LogistSettings.FileKey;
import logist.LogistSettings.FlagKey;
import logist.LogistSettings.SizeKey;
import logist.LogistSettings.TimeoutKey;

class SettingsParser {

	SettingsParser() {
	}

	LogistSettings parse(XMLTag settingsTag) throws ParserException {
		LogistSettings settings = new LogistSettings();

		for (Entry<String, String> entry : Parsers.parseMap(settingsTag)
				.entrySet()) {
			String name = entry.getKey().toUpperCase().replace('-', '_');

			// parse colors
			if (name.startsWith("COLOR_")) {
				ColorKey key = XMLTag.convert(name.substring(6), settingsTag, ColorKey.class);
				Color value = XMLTag.convert(entry.getValue(), key, Color.class);
				settings.set(key, value);
			}
			// parse timeouts
			else if (name.startsWith("TIMEOUT_")) {
				TimeoutKey key = XMLTag.convert(name.substring(8), settingsTag, TimeoutKey.class);
				long value = XMLTag.convert(entry.getValue(), key, Long.class);
				settings.set(key, value);
			}
			// parse timeouts
			else if (name.startsWith("FILE_")) {
				FileKey key = XMLTag.convert(name.substring(5), settingsTag, FileKey.class);
				File value = XMLTag.convert(entry.getValue(), key, File.class);
				settings.set(key, value);
			}
			// parse flags
			else if (name.startsWith("FLAG_")) {
				FlagKey key = XMLTag.convert(name.substring(5), settingsTag, FlagKey.class);
				boolean value = XMLTag.convert(entry.getValue(), key, Boolean.class);
				settings.set(key, value);
			}
			// parse sizes
			else {
				SizeKey key = XMLTag.convert(name, settingsTag, SizeKey.class);
				int value = XMLTag.convert(entry.getValue(), key, Integer.class);
				settings.set(key, value);
			}
		}
		return settings;
	}
}
