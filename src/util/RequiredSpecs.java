package util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class with the fields required by the client
 */
public class RequiredSpecs {
	private int memory;
	private int cpu;
	private int time;
	
	public RequiredSpecs(){
		this.memory = 0;
		this.cpu = 0;
		this.time = 0;
	}
	
	public RequiredSpecs(int memory, int cpu, int time) {
		this.memory = memory;
		this.cpu = cpu;
		this.time = time;
	}
	
	/*
	 * Constructor receiving the field in a string in JSON format
	 */
	public RequiredSpecs(String specs){
		JSONParser parser = new JSONParser();
		JSONObject content;
		try {
			content = (JSONObject) parser.parse(specs);
			this.memory = ((Long)content.get("memoryNeeded")).intValue();
			this.cpu = ((Long)content.get("cpuNeeded")).intValue();
			this.time = ((Long)content.get("timeNeeded")).intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public RequiredSpecs(RequiredSpecs specs) {
		this.memory = specs.getMemory();
		this.cpu = specs.getCpu();
		this.time = specs.getTime();
	}

	/*
	 * Convert object to string in JSON format
	 */
	@Override
	public String toString() {
		JSONObject messageContent = new JSONObject();
		messageContent.put("memoryNeeded", this.memory);
		messageContent.put("cpuNeeded", this.cpu);
		messageContent.put("timeNeeded", this.time);
		return messageContent.toJSONString();
	}

	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getCpu() {
		return cpu;
	}

	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
}
