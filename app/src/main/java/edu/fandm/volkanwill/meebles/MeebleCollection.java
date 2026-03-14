package edu.fandm.volkanwill.meebles;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MeebleCollection implements Serializable {
    private Map<Character, Integer> meeblesByEnv;

    public MeebleCollection() {
        meeblesByEnv = new HashMap<>();
    }

    public void addMeebles(char envType, int count) {
        meeblesByEnv.put(envType, meeblesByEnv.getOrDefault(envType, 0) + count);
    }

    public void removeMeebles(char envType, int count) {
        meeblesByEnv.put(envType, Math.max(0, meeblesByEnv.getOrDefault(envType, 0) - count));
    }

    public int getMeebles(char envType) {
        return meeblesByEnv.getOrDefault(envType, 0);
    }

    public Map<Character, Integer> getAll() {
        return meeblesByEnv;
    }
}
