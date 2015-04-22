package it114112fyp.util;

import java.util.ArrayList;
import java.util.HashMap;

public class DataList extends ArrayList<HashMap<String, Object>> {

    public DataList() {
    }

    public DataList(DataList dataList) {
        for (HashMap<String, Object> x : dataList) {
            add(x);
        }
    }

    public int idOfIndex(int index) {
        return (Integer) get(index).get("id");
    }

    public int indexOfId(Integer id) {
        return indexOf(findById(id));
    }

    public int indexOfId(int id) {
        return indexOfId(Integer.valueOf(id));
    }

    public HashMap<String, Object> findById(Integer id) {
        for (HashMap<String, Object> x : this) {
            if (x.get("id").equals(id))
                return x;
        }
        return null;
    }

    public HashMap<String, Object> findById(int id) {
        return findById(Integer.valueOf(id));
    }
}
