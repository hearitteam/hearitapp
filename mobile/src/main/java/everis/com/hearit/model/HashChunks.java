package everis.com.hearit.model;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by mauriziomento on 09/05/17.
 */
public class HashChunks extends SugarRecord {

    private String hash;

    public HashChunks() {
    }

    public HashChunks(String hash) {
        this.hash = hash;
    }

    public List<Sound> getSounds(String hash) {
        return Sound.find(Sound.class, "hash = ?", hash);
    }
}
