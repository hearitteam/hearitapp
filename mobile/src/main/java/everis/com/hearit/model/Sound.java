package everis.com.hearit.model;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class Sound extends SugarRecord {

    @Unique
    private String hash;
    @Unique
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
}
