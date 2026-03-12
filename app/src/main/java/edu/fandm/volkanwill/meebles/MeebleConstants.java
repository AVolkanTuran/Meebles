package edu.fandm.volkanwill.meebles;

public class MeebleConstants {

    public static final char ENV_VOLCANO = '1';
    public static final char ENV_FOREST = '2';
    public static final char ENV_DESERT = '3';
    public static final char ENV_TUNDRA = '4';

    public static final char VILLAGE = '1';
    public static final char TOWN = '2';
    public static final char CITY = '3';

    public static String envTypeToString(char envType) {
        switch (envType) {
            case ENV_VOLCANO: return "Volcano";
            case ENV_FOREST:  return "Forest";
            case ENV_DESERT:   return "Desert";
            case ENV_TUNDRA:   return "Tundra";
            default:          return "Unknown";
        }
    }

    public static String cityTypeToString(char cityType) {
        switch (cityType) {
            case VILLAGE: return "Village";
            case TOWN: return "Town";
            case CITY: return "City";
            default:         return "Unknown";
        }
    }
}
