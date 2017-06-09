package subpack;

import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

public class TabHeaderRenderer extends DefaultTableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable tab, Object v, boolean selected, boolean focus, int r, int c){
        JLabel l=(JLabel)(super.getTableCellRendererComponent(tab, v, selected, focus, r, c));

        //Сбрасываем параметры заголовка столбца в параметры по-умолчанию
        l.setBackground(Color.LIGHT_GRAY);
        l.setBorder(BorderFactory.createEtchedBorder());
        l.setIcon(new ImageIcon(""));

        TabModel tm=(TabModel)(tab.getModel());
        if(c==tm.getTypeSort()){
            if(tm.getDirectionSort()==1)l.setIcon(new ImageIcon("icons\\sort_asc.png"));
            if(tm.getDirectionSort()==(-1))l.setIcon(new ImageIcon("icons\\sort_des.png"));
        }
        return l;
    }

}
