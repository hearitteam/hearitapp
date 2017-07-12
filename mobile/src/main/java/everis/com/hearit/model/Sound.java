package everis.com.hearit.model;

import com.orm.SugarRecord;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class Sound extends SugarRecord {

    private String hash;
    private String name;

    private int importance;

    public Sound() {
    }

    public Sound(String name) {
        this.name = name;
        this.importance = 0;
    }

    public Sound(String name, int importance) {
        this.name = name;
        this.importance = importance;
    }

    public Sound(String hash, String name, int importance) {
        this.hash = hash;
        this.name = name;
        this.importance = importance;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int[] getArrayHash() {
        String[] hashSplit = this.hash.split(" ");

        int[] arrayHash = new int[hashSplit.length];
        for(int i = 0; i < hashSplit.length; i++){
            arrayHash[i] = Integer.parseInt(hashSplit[i]);
        }

        return arrayHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    @Override
    public int hashCode() {
        if (getHash() != null && getName() != null) {
            return getHash().hashCode() + getName().hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if(!Sound.class.isInstance(o)){
            return  false;
        }

        Sound a = (Sound) o;
        boolean hash = a.getHash().equals(getHash());
        boolean name = a.getName().equals(getName());
        return hash && name;
    }
}
