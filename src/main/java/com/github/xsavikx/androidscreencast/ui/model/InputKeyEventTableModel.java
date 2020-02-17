package com.github.xsavikx.androidscreencast.ui.model;

import com.github.xsavikx.androidscreencast.api.injector.InputKeyEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class InputKeyEventTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1553313932570896541L;
    private final static String INDEX_COLUMN_NAME = "#";
    private final static String TITLE_COLUMN_NAME = "title";
    private final static String DESCRIPTION_COLUMN_NAME = "description";
    static final String[] COLUMN_NAMES = {
            INDEX_COLUMN_NAME, TITLE_COLUMN_NAME, DESCRIPTION_COLUMN_NAME
    };

    private List<List<Object>> data = new ArrayList<>();
    private int rowCount = 0;

    @Inject
    InputKeyEventTableModel(InputKeyEvent[] initialData) {
        initData(initialData);
    }

    private void initData(InputKeyEvent[] inputKeyEvents) {
        for (InputKeyEvent e : inputKeyEvents) {
            data.add(getDataRow(e));
        }
    }

    private List<Object> getDataRow(InputKeyEvent e) {
        List<Object> row = new ArrayList<>();
        row.add(rowCount++);
        row.add(e.name());
        row.add(e.getDescription());
        return row;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex).get(columnIndex);
    }
}
