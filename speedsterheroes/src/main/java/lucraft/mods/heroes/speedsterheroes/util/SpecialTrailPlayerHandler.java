package lucraft.mods.heroes.speedsterheroes.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;

public class SpecialTrailPlayerHandler {

	public static ArrayList<String> specialUUIDs = new ArrayList<String>();
	
	public static void init() {
		URL url = null;
		try {
			url = new URL("https://drive.google.com/uc?export=download&id=0B6_wwPkl6fmOaHpaQlMxMW9pTm8");
		} catch (MalformedURLException e) {
			LucraftCoreUtil.logInfo(SpeedsterHeroes.NAME, "Could not connect to read special trail UUIDs");
		}
		
		Scanner s = null;
		try {
			s = new Scanner(url.openStream());
		} catch (IOException e) {
			LucraftCoreUtil.logInfo(SpeedsterHeroes.NAME, "Could not read special trail UUIDs");
		}
		
		while(url != null && s != null && s.hasNextLine()) {
			String uuid = s.nextLine();
			
			if(!uuid.startsWith("#"))
				specialUUIDs.add(uuid);
		}
		
//		// TheLucraft
//		specialUUIDs.add("0669d99d-b34d-40fc-a4d8-c7ee963cc842");
//		// Crafter_P
//		specialUUIDs.add("c4ebc10b-0142-49a2-bf85-ddaf36059e4b");
//		// _Maxmos_
//		specialUUIDs.add("4d4b3b1c-c141-4dda-9329-cacc33f1ea30");
//		// TheMagicFlux
//		specialUUIDs.add("bcce55fb-9616-4d99-9ded-ecffa42622cc");
//		// LeDropsHD
//		specialUUIDs.add("b2bac2f6-93ab-4ef6-8684-4c287e5a1f6a");
//		// Sheriff1999
//		specialUUIDs.add("3fa3dc7d-3de2-4ba1-a0ca-adc57bf0827d");
//		// HerrBergmann
//		specialUUIDs.add("3ce22eb8-41de-476c-b0b0-c0528a2abc0c");
//		// CrimsonMist
//		specialUUIDs.add("07d14d8a-6e73-489c-a838-f9ae94a432e2");
//		// skulavatar
//		specialUUIDs.add("3ecbd389-00a1-43e9-93b3-53d4e4b97f70");
//		// MackleMoo
//		specialUUIDs.add("99fc159d-dd65-44b6-8674-3890eede3d1e");
//		// Xelron
//		specialUUIDs.add("19f8c62d-0f67-4e82-94ad-e8623369ac5b");
//		// TheCrafterM
//		specialUUIDs.add("4ca314e8-dded-4346-b8b0-aaefa2f43f08");
//		// ichbins001
//		specialUUIDs.add("97a15a50-098e-4421-b5ff-21c6dc331fe2");
//		// NylonBoy
//		specialUUIDs.add("3f223a40-646f-4f34-834e-0a7be1f69b69");
//		// Creeper_E
//		specialUUIDs.add("3e1be1b1-c9a6-4634-b87c-ec85ef11dbd6");
//		// justin250100
//		specialUUIDs.add("de615818-518c-4c55-8b8c-243f87481b28");
//		// Tommy
//		specialUUIDs.add("bd1b5129-150f-46e9-baeb-72be2668213d");
//		// EmeraldArcher48
//		specialUUIDs.add("c4f631de-9267-4dd1-b219-b6fb91816421");
//		// ChoppeKanonen
//		specialUUIDs.add("c6caae2a-755f-403f-bd26-a041e785c874");
//		// Z3R0X
//		specialUUIDs.add("383f4630-f10c-41a3-818a-41fbd77c8822");
//		// FiskFille
//		specialUUIDs.add("52d1e4a0-062a-4623-8ac9-4f9ee790f40d");
//		// Tihyo
//		specialUUIDs.add("20b3bed1-a4a3-4da6-aafb-c4e425e9a3ce");
//		// AFlyingGrayson
//		specialUUIDs.add("fa396f29-9e23-479b-93a5-43e0780f1453");
//		// LeKoopa92
//		specialUUIDs.add("b0bb3fff-ddbd-40c6-95d7-51dfcaece879");
	}
	
}
