package everis.com.hearit.model;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

/**
 * Created by mauriziomento on 16/05/17.
 */
public class HiSoundView extends SugarRecord {

    @Unique
    private String name;
    private int importance;

    public HiSoundView() {
    }

    public HiSoundView(String name) {
        this.name = name;
        this.importance = 0;
    }

    public HiSoundView(String name, int importance) {
        this.name = name;
        this.importance = importance;
    }

    public HiSoundView(String hash, String name, int importance) {
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
