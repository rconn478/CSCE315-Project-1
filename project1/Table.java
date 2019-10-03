package project1;

import java.io.Serializable;
import java.sql.Array;
import project1.conditional.*;
import java.lang.System;
import java.util.*;
import java.util.Map.Entry;
import javafx.util.Pair;

public class Table implements Serializable {

    //CLASS FIELDS
    private String name;
    private ArrayList<String> attributeNames;
    private ArrayList<String> attributeTypes;
    private ArrayList<Integer> pKeyIndices;
    private HashMap<ArrayList<String>,ArrayList<String>> entries; //key is the list of primary keys, value is a list of all the attributes
  
    //CLASS CONSTRUCTORS
    public Table(String name, ArrayList<String> attributeNames, ArrayList<String> attributeTypes, ArrayList<Integer> pKeyIndices) {
        this.name = name;
        this.attributeNames = attributeNames;
        this.attributeTypes = attributeTypes;
        this.pKeyIndices = pKeyIndices;
        this.entries = new HashMap<>();
        this.pKeyIndices.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
    }

    public Table(String name, ArrayList<Column> cols, ArrayList<Column> pKeys)
    {
        this.name = name;
        this.pKeyIndices = new ArrayList<>();
        this.attributeNames = new ArrayList<>();
        this.attributeTypes = new ArrayList<>();
        this.entries = new HashMap<>();

        for (int i = 0; i < cols.size(); ++i)
        {
            attributeNames.add(cols.get(i).colName);
            attributeTypes.add(cols.get(i).colType);
            if (pKeys.contains(cols.get(i)))
            {
                this.pKeyIndices.add(i);
            }
        }
    }

    public Table(Table a) {
        this.name = a.name;
        this.attributeTypes = a.attributeTypes;
        this.attributeNames = a.attributeNames;
        this.pKeyIndices = a.pKeyIndices;
        this.entries = a.entries;
    }

    public Table() {
        this.name = "";
        this.attributeNames = new ArrayList<>();
        this.attributeTypes = new ArrayList<>();
        this.pKeyIndices = new ArrayList<>();
        this.entries = new HashMap<>();
    }

    //GETTERS AND SETTERS
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<ArrayList<String>, ArrayList<String>> getEntries() {
        return entries;
    }

    public void setEntries(HashMap<ArrayList<String>, ArrayList<String>> entries) { this.entries = entries; }

    public ArrayList<String> getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(ArrayList<String> attributeTypes) {
        this.attributeTypes = attributeTypes;
    }

    public ArrayList<String> getAttributeNames() { return attributeNames; }

    public void setAttributeNames(ArrayList<String> newNames){ this.attributeNames = newNames; }

    public ArrayList<Integer> getPKeyIndices() { return pKeyIndices; }

    public void setpKeyIndices(ArrayList<Integer> pKeyIndices) { this.pKeyIndices = pKeyIndices; }

    public int getSize() { return entries.size(); }

    public ArrayList<String> getpKeyNames() {
        ArrayList<String> pKeyNames = new ArrayList<>();
        for (Integer i : pKeyIndices) {
            String pKeyName = attributeNames.get(i);
            pKeyNames.add(pKeyName);
        }
        return pKeyNames;
    }

    //CLASS FUNCTIONS
    //TODO: error checking
    public int getPKeyIndex(int attIndex) {
        int keyIndex = -1;
        for(int i = 0 ; i <= attIndex ; i++) {
            if(pKeyIndices.contains(i)) {
                keyIndex++;
            }
        }
        return keyIndex;
    }


    //TODO: reject attributes if there's already an element with pKeys
    public void addEntry(ArrayList<String> attributes) {
        ArrayList<String> pKeys = new ArrayList<>();
        if(pKeyIndices.size() != 0) {
            for (int i = 0; i < pKeyIndices.size(); i++) {
                int keyIndex = pKeyIndices.get(i);
                String pKey = attributes.get(keyIndex);
                pKeys.add(pKey);
            }
        } else {
            pKeys = attributes;
        }
        //don't add to table if primary key already exists
        if(entries.containsKey(pKeys)){
            return;
        }
        entries.put(pKeys, attributes);
    }

    public void deleteEntry(ArrayList<String> key){
        entries.remove(key);
    }

    public ArrayList<Column> getPrimaryKeys() {
        ArrayList<Column> pKeys = new ArrayList<>();
        for (Integer i : pKeyIndices) {
            pKeys.add(new Column(attributeNames.get(i), attributeTypes.get(i)));
        }
        return pKeys;
    }

    public ArrayList<Column> getAllColumns() {
        ArrayList<Column> cols = new ArrayList<>();
        for (int i = 0; i < attributeNames.size(); ++i) {
            cols.add(new Column(attributeNames.get(i), attributeTypes.get(i)));
        }
        return cols;
    }

    public boolean contains(ArrayList<String> attributes) {
        return entries.containsValue(attributes);
    }

    public Table filter(Conditional cond) throws IncompatibleTypesException {
        Table results = new Table("temp", this.getAllColumns(), this.getPrimaryKeys());
        for (Entry<ArrayList<String>,ArrayList<String>> entry : entries.entrySet()) {
            ArrayList<String> row = entry.getValue();
            ArrayList<Cell> cells = getRow(row);
            try {
                if (cond.SelectsEntry(cells)) {
                    results.addEntry(row);
                }
            }
            catch (FieldNotInTableException ex) {
                System.out.println("[INFO] The command looked for a field that isn't in the tabl!!!");
                System.out.println(ex);
            }
        }
        return results;
    }

    public ArrayList<Cell> getRow(ArrayList<String> row) {
        ArrayList<Cell> output = new ArrayList<>();
        for (int i=0; i<attributeNames.size(); i++) {
            Cell c1 = new Cell();
            c1.fieldName = attributeNames.get(i);
            c1.fieldType = attributeTypes.get(i);
            c1.fieldValue = row.get(i);
            output.add(c1);
        }
        return output;
    }

    public void updateRow(ArrayList<String> pKeys, ArrayList<Pair<String,String>> updates) {
        //Map.Entry<ArrayList<String>, ArrayList<String>>.get(pKeys);
        ArrayList<String> attributes =  entries.get(pKeys);
        entries.remove(pKeys);
        for(Pair<String,String> update : updates) {
            int attIndex = attributeNames.indexOf(update.getKey());
            boolean isKey = pKeyIndices.contains(attIndex);
            attributes.set(attIndex, update.getValue());
            if(isKey) {
                int keyIndex = getPKeyIndex(attIndex);
                pKeys.set(keyIndex, update.getValue());
            }
        }
        entries.put(pKeys, attributes);

    }

    public String showTable() {
        String toShow = this.name + ":\n";
        for(String s : attributeNames) {
            toShow = toShow + s + " ";
        }
        toShow += "\n";
        for(HashMap.Entry<ArrayList<String>,ArrayList<String>> entry : this.entries.entrySet()) {
            for(int j = 0; j < entry.getValue().size(); j++) {
                toShow = toShow + entry.getValue().get(j) + " ";
            }
            toShow = toShow + "\n";
        }
        return toShow;
    }
}