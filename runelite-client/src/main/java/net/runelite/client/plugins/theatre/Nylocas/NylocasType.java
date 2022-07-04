package net.runelite.client.plugins.theatre.Nylocas;

import java.util.HashMap;

enum NylocasType {
    MELEE_SMALL(8342, 10774, 10791, 8348, 10780, 10797),
    MELEE_BIG(8345, 10777, 10794, 8351, 10783, 10800),
    RANGE_SMALL(8343, 10775, 10792, 8349, 10781, 10798),
    RANGE_BIG(8346, 10778, 10795, 8352, 10784, 10801),
    MAGE_SMALL(8344, 10776, 10793, 8350, 10782, 10799),
    MAGE_BIG(8347, 10779, 10796, 8353, 10785, 10802);

    private int id;

    private int id_sm;

    private int id_hm;

    private int aggroId;

    private int aggroId_sm;

    private int aggroId_hm;

    private static final HashMap<Integer, NylocasType> lookupMap;

    public int getId() {
        return this.id;
    }

    public int getId_sm() {
        return this.id_sm;
    }

    public int getId_hm() {
        return this.id_hm;
    }

    public int getAggroId() {
        return this.aggroId;
    }

    public int getAggroId_sm() {
        return this.aggroId_sm;
    }

    public int getAggroId_hm() {
        return this.aggroId_hm;
    }

    public static HashMap<Integer, NylocasType> getLookupMap() {
        return lookupMap;
    }

    static {
        lookupMap = new HashMap<>();
        for (NylocasType v : values()) {
            lookupMap.put(Integer.valueOf(v.getId()), v);
            lookupMap.put(Integer.valueOf(v.getId_sm()), v);
            lookupMap.put(Integer.valueOf(v.getId_hm()), v);
            lookupMap.put(Integer.valueOf(v.getAggroId()), v);
            lookupMap.put(Integer.valueOf(v.getAggroId_sm()), v);
            lookupMap.put(Integer.valueOf(v.getAggroId_hm()), v);
        }
    }

    NylocasType(int id, int id_sm, int id_hm, int aggroId, int aggroId_sm, int aggroId_hm) {
        this.id = id;
        this.id_sm = id_sm;
        this.id_hm = id_hm;
        this.aggroId = aggroId;
        this.aggroId_sm = aggroId_sm;
        this.aggroId_hm = aggroId_hm;
    }
}
