package org.xbib.analyzer.marc.zdb.bib;

import org.xbib.etl.marc.MARCEntity;
import org.xbib.etl.marc.MARCEntityQueue;
import org.xbib.marc.FieldList;

import java.io.IOException;
import java.util.Map;

public class RecordLeader extends MARCEntity {

    private Map<String,Object> codes;

    private String predicate;

    public RecordLeader(Map<String,Object> params) {
        super(params);
        this.codes= (Map<String,Object>)params.get("codes");
        this.predicate = params.containsKey("_predicate") ?
                        (String)params.get("_predicate") : "leader";
    }

    @Override
    public boolean fields(MARCEntityQueue.MARCWorker worker, FieldList fields) throws IOException {
        if (codes == null) {
            return false;
        }
        String value = fields.getLast().data();
        for (Map.Entry<String,Object> entry : codes.entrySet()) {
            String k = entry.getKey();
            int pos = Integer.parseInt(k);
            Map<String,String> v = (Map<String,String>)codes.get(k);
            String code = value.length() > pos ? value.substring(pos,pos+1) : "";
            if (v.containsKey(code)) {
                worker.getWorkerState().getResource().add(predicate, v.get(code));
            }
        }
        // deleted record?


        char ch6 = value.charAt(6);
        char ch7 = value.charAt(7);

        boolean isBook = (ch6 == 'a' || ch6 == 't') &&
                (ch7 == 'a' || ch7 == 'c' || ch7 == 'd' || ch7 == 'm');
        if (isBook) {
            worker.getWorkerState().getResource().add("type", "book");
        }

        boolean isComputerFile = ch6 == 'm';
        if (isComputerFile) {
            worker.getWorkerState().getResource().add("type", "computerfile");
        }

        boolean isMap = (ch6 == 'e' || ch6 == 'f');
        if (isMap) {
            worker.getWorkerState().getResource().add("type", "map");
        }

        boolean isMusic = (ch6 == 'c' || ch6 == 'd' || ch6 == 'i' || ch6 == 'j');
        if (isMusic) {
            worker.getWorkerState().getResource().add("type", "music");
        }

        boolean isContinuingResource = ch6 == 'a' &&
                (ch7 == 'b' || ch7 == 'i' || ch7 == 's');
        if (isContinuingResource) {
            worker.getWorkerState().getResource().add("type", "continuingresource");
        }

        boolean isVisualMaterial = (ch6 == 'g' || ch6 == 'k' || ch6 == 'o' || ch6 == 'r');
        if (isVisualMaterial) {
            worker.getWorkerState().getResource().add("type", "visualmaterial");
        }

        boolean isMixedMaterial = ch6 == 'p';
        if (isMixedMaterial) {
            worker.getWorkerState().getResource().add("type", "mixedmaterial");
        }
        return true;
    }
}
