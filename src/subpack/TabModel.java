package subpack;

import javax.swing.table.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;

public class TabModel extends AbstractTableModel{

    private final String[] colNames={"Имя", "Тип", "Размер", "Дата создания", "Дата изменения"};    //Имена столбцов

    private int typeSort=0;        //Тип сортировки
    private int directionSort=1;   //Направление сортировки: 1 - по возрастанию, (-1) - по убыванию

    private int folderPos=(-1);    //Граница списка папок в списках атрибутов

    private final List[] cols=new List[colNames.length];    //Массив, содержащий столбцы таблицы

    public TabModel(File folder, boolean hiddenEnabled) {
        super();
        for(int i=0;i<cols.length;i++)cols[i]=new LinkedList<Comparable>();
        refresh(folder, hiddenEnabled);
    }

    //Метод обновляет содержимое таблицы, извлекая данные из объекта folder
    public final void refresh(File folder, boolean hiddenEnabled){
        //Сбасываем значения атрибутов файлов
        for(int i=0;i<cols.length;i++)cols[i].clear();
        folderPos=(-1);

        //Получаем список файлов
        File[] lst=folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isHidden()?hiddenEnabled:true;
            }
        });
        if(lst==null){
            fireTableDataChanged();
            return;
        }
        if(lst.length==0){
            fireTableDataChanged();
            return;
        }

        //Формируем списки атрибутов
        Date dc;    //Даты создания
        Date dm;    //Даты модификации
        for(File f: lst){
            try {
                dc=new Date(((FileTime)Files.getAttribute(f.toPath(), "creationTime")).toMillis());
            } catch (IOException ex) {
                dc=new Date(0);
            }
            try {
                dm=new Date(((FileTime)Files.getAttribute(f.toPath(), "lastModifiedTime")).toMillis());
            } catch (IOException ex) {
                dm=new Date(0);
            }
            if(f.isDirectory()){
                folderPos++;
                cols[0].add(folderPos, f);
                cols[1].add(folderPos, "<DIR>");
                cols[2].add(folderPos, new Long(-1));
                cols[3].add(folderPos, dc);
                cols[4].add(folderPos, dm);
            }
            if(f.isFile()){
                //Определяем тип файла - то есть его расширение
                String nameFile=f.getName();
                String extendFile="";
                int dotPos=nameFile.lastIndexOf(".");
                if((dotPos==(-1)) | (dotPos==0) | (dotPos==(nameFile.length()-1)))extendFile=""; else extendFile=nameFile.substring(dotPos+1);
                cols[0].add(f);
                cols[1].add(extendFile);
                cols[2].add(f.length());
                cols[3].add(dc);
                cols[4].add(dm);
            }
        }
        sort(-1);
    }

    //Метод сортирует таблицу
    public void sort(int ts){

        //Если ts==(-1), то метод вызван из метода refresh и осуществить сортировку нужно используя текущие параметры
        if(ts!=(-1)){
            if(ts==typeSort)directionSort*=(-1);
            if(ts!=typeSort)directionSort=1;
            typeSort=ts;
        }

        int[] range=new int[4];
        range[0]=0; range[1]=folderPos; range[2]=folderPos+1; range[3]=cols[0].size()-1;
        int start;
        int end;
        boolean isSwap;
        Comparable a;
        Comparable b;
        int c;
        for(int i=0; i<3;i+=2){
            start=range[i];
            end=range[i+1];
            isSwap=true;
            while (isSwap) {
                isSwap=false;
                for(int j=start; j<end; j++){
                    a=(Comparable) cols[typeSort].get(j);
                    b=(Comparable) cols[typeSort].get(j+1);
                    c=a.compareTo(b);
                    if(c!=0)c=c/Math.abs(c);
                    if(c==directionSort){
                        for(int t=0;t<cols.length;t++){
                            a=(Comparable) cols[t].get(j);
                            b=(Comparable) cols[t].get(j+1);
                            cols[t].set(j, b);
                            cols[t].set(j+1, a);
                        }
                        isSwap=true;
                    }
                }
            }
        }

        //Оповещаем слушателей об обновлении модели данных
        fireTableDataChanged();
    }

    //Метод возвращает тип текущей сортировки
    public int getTypeSort(){
        return typeSort;
    }

    //Метод возвращает направление текущей сортировки
    public int getDirectionSort(){
        return directionSort;
    }

    @Override
    public int getRowCount() {
        return cols[0].size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column){
        return colNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(columnIndex>=0 & columnIndex<cols.length){
            return cols[columnIndex].get(rowIndex);
        }
        return "null";
    }

}
