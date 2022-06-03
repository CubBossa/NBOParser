package de.cubbossa.nbo.nbtconverter;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTContainer;
import nbo.NBOSerializer;

import java.util.Map;

public class NBOToNBTSerializer {


	public static void addNBTSerialization(NBOSerializer serializer) {

	}

	private static NBTCompound serialize(Map<String, Object> map) {
		return new NBTContainer();
	}

}
