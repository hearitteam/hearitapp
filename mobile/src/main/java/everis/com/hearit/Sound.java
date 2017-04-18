package everis.com.hearit;

import com.orm.SugarRecord;

/**
 * Created by mauriziomento on 21/02/16.
 */
public class Sound extends SugarRecord {
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
