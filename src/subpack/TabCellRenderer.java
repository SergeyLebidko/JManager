package subpack;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.text.*;
import java.util.Date;
import javax.swing.table.DefaultTableCellRenderer;

public class TabCellRenderer extends DefaultTableCellRenderer{

    @Override
    public Component getTableCellRendererComponent(JTable tab, Object v, boolean selected, boolean focus, int r, int c){
        JLabel l=(JLabel)(super.getTableCellRendererComponent(tab, v, selected, focus, r, c));

        //Сбрасываем параметры отображения
        l.setForeground(new Color(0,0,0));
        l.setIcon(new ImageIcon(""));

        //Если столбец отображает список файлов
        if(c==0){
            File f=(File)v;
            String nameFile=f.getName();

            //Если ячейка отображает имя файла, то отделяем имя от расширения
            if(f.isFile()){
                int dotPos=nameFile.lastIndexOf(".");
                if(dotPos!=(-1) & dotPos!=0 & dotPos!=nameFile.length())nameFile=nameFile.substring(0, dotPos);
            }
            l.setText(nameFile);

            //Устанавливаем цвет и значок в зависимости от того, скрыты файл/папка или не скрыты
            if(f.isHidden())l.setForeground(new Color(100,100,100));
            if(f.isDirectory() & f.isHidden())l.setIcon(new ImageIcon("icons\\hidden_folder.png"));
            if(f.isDirectory() & !f.isHidden())l.setIcon(new ImageIcon("icons\\folder.png"));
        }

        //Обработка вывода размера файлов
        if(c==2){
            Long val=(Long)v;
            if(val==(-1))l.setText("");
            if(val!=(-1)){
                NumberFormat nf=NumberFormat.getInstance();
                l.setText(nf.format(val));
            }
        }

        //Обработка дат
        if(c==3 | c==4){
            Date d=(Date)v;
            DateFormat df=DateFormat.getInstance();
            l.setText(df.format(d));
        }

        return l;
    }

}
