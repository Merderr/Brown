package net.runelite.client.plugins.theatre.Nylocas;

import java.util.Objects;

class NyloNPC {
    private NylocasType nyloType;

    private NylocasSpawnPoint spawnPoint;

    public void setNyloType(NylocasType nyloType) {
        this.nyloType = nyloType;
    }

    public void setSpawnPoint(NylocasSpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public void setAggressive(boolean aggressive) {
        this.aggressive = aggressive;
    }

    public String toString() {
        return "NyloNPC(nyloType=" + getNyloType() + ", spawnPoint=" + getSpawnPoint() + ", aggressive=" + isAggressive() + ")";
    }

    public NylocasType getNyloType() {
        return this.nyloType;
    }

    public NylocasSpawnPoint getSpawnPoint() {
        return this.spawnPoint;
    }

    private boolean aggressive = false;

    public boolean isAggressive() {
        return this.aggressive;
    }

    NyloNPC(NylocasType nyloType, NylocasSpawnPoint nylocasSpawnPoint) {
        this.nyloType = nyloType;
        this.spawnPoint = nylocasSpawnPoint;
    }

    NyloNPC(NylocasType nyloType, NylocasSpawnPoint nylocasSpawnPoint, boolean aggressive) {
        this(nyloType, nylocasSpawnPoint);
        this.aggressive = aggressive;
    }

    public boolean equals(Object other) {
        if (!(other instanceof NyloNPC))
            return false;
        NyloNPC otherNpc = (NyloNPC)other;
        return (this.nyloType.equals(otherNpc.getNyloType()) && this.spawnPoint.equals(otherNpc.getSpawnPoint()));
    }

    public int hashCode() {
        return Objects.hash(new Object[] { this.nyloType, this.spawnPoint });
    }
}
