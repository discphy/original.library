package com.eliall.process;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.eliall.common.Config;
import com.eliall.common.EliObject;
import com.eliall.util.Tool;

@SuppressWarnings("rawtypes")
public class Upload {
	public EliObject files(EliObject parameters) {
		EliObject files = null, file = null;
		
		if (parameters.get(Config.FILES_KEY) == null) return null;
		else files = new EliObject();
		
		for (Object object : parameters.getList(Config.FILES_KEY)) files.put(Tool.nvl((file = new EliObject(object)).remove("name")), file); 

		return files;
	}
	
	public EliObject mapping(EliObject file, Map map) {
		EliObject temp = null;

		if (map == null || map.size() <= 0) return file;
		else temp = new EliObject(file);

		for (String key : temp.keySet()) if (map.get(key) != null) file.put((String)map.get(key), temp.get(key));
			
		return file;
	}
	
	public boolean save(EliObject file, String path, String name) {
		Path source = null, target = null;
		String extension = null;

		source = Paths.get(file.getString("path", ""));
		extension = (extension = file.getString("file", "")).substring(extension.lastIndexOf(".") + 1, extension.length());
		target = Paths.get(path, name.replaceAll("\\$\\{extension\\}", extension));

		try {
			Files.createDirectories(target.getParent());
			Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);

			file.put("save", target.toString());
			file.put("size", Files.size(target));
		} catch (Throwable e) { return false; }
		
		return Files.exists(target);
	}
}