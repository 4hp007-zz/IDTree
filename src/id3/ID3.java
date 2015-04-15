

/*
Ussage:
ID3 x = new ID3("//D:/some path/db.accdb","table name");
x.start().print();
*/

package id3;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class ID3 {

    String dbPath, table, play;
    Connection conn;
    Tree<String>[] col;
    Tree<String> root;

    public ID3(String dbPath, String table) {
        this.dbPath = dbPath;
        this.table = table;        
    }
    
//Getting Column names
    
    public void colls() {
        try {
            
            ResultSet rs = connection();
            ResultSetMetaData rm = rs.getMetaData();            
            play = rm.getColumnName(rm.getColumnCount());           
            col = new Tree[rm.getColumnCount() - 1];            
            for (int i = 0; i < col.length; i++)
                col[i] = new Tree(rm.getColumnName(i + 1));            
            while (rs.next()) {
                for (Tree<String> col1 : col) {
                    String x = rs.getString(col1.data);
                    boolean flag = true;
                    for (Tree<String> children : col1.children)
                        if (children.data.equals(x))
                            flag = false;
                    if (flag)
                        col1.addChild(x);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Call to create tree
    public Tree<String> start() {
        colls();
        int a = run(null);
        root = new Tree<>(col[a].data);      
        Queue<Tree<String>> alt = new LinkedList<>();
       
        for (Tree<String> tree : col[a].children) {
            root.addChild(tree.data);
        }
        
        alt.add(root);
        while(!alt.isEmpty()){           
            for (Tree<String> child : alt.remove().children) {             
                a = run(child);             
                if(a==-1)
                    child.addChild("yes");
                else if(a==-2)
                    child.addChild("no");                
                else if(a==-4){                  
                    child.addChild("NULL");
                }                
                else{
                    Tree temp = child.addChild(col[a].data);
                    for (Tree<String> tree : col[a].children) {
                        temp.addChild(tree.data);
                    }
                    alt.add(temp);
                }
            }                        
        }                     
        return root;

    }

    //Creates where clause for query
    static String where(Tree<String> n) {

        if(n==null)
            return "";        
        String x = " where ";        
        while (true) {
            String a = n.parent.data;
            String b = n.data;
            x += a + " = '" + b + "'";
            if (n.parent.parent == null)
                return x;
            x += " and ";
            n = n.parent.parent;
        }
    }

    //Selects most feasible attribute
    int run(Tree<String> wh) {

        String x = where(wh);
        String[] c = x.split(" ");        
        int rs = connection(x);  
        if(rs==0){
            return -4;
        }
        if(x.equals(""))
            x+=" where ";
        else
            x+=" and ";
        
        double h1 = calc(x,rs);
        double y = connection(x+" "+play+" = 'yes'");
        if(c.length>=4*col.length)
            if(y>=rs-y)
                return -1;        
            else 
                return -2;
        if(h1==0){
            
            if(y==0)
                return -2;        
            else if(y==rs)
                return -1;
        }
        double gain = Double.POSITIVE_INFINITY;
        int count = 0;        
        for (int i = 0; i < col.length; i++) {
            
            if (!x.contains(col[i].data)) {
                double o = 0, h;
                for (int j = 0; j < col[i].children.size(); j++) {                   
                    h = calc(x + col[i].data + " = '" + col[i].children.get(j).data + "' and ", rs);                    
                    o += h;                    
                }                
                if (gain > o) {
                    gain = o;
                    count = i;
                }
                
            }
        }
        if(h1==gain){           
             if(y>=rs-y)
                return -1;        
            else 
                return -2;   
        }

        return count;        
    }

    //Calcuates Entropy
    double calc(String a, double t) {
        
        double y = connection(a+" "+play+" = 'yes'");
        double n = connection(a+" "+play+" = 'no'");
        if (y == 0 && n == 0) 
            return 0;        
        else if(y==0 || n==0){
            return 0;
        }
        double et = -(y / (y + n) * Math.log(y / (y + n)) / Math.log(2)) - (n / (y + n) * Math.log(n / (y + n)) / Math.log(2));
        return ((y + n) / t) * et;

    }

    int connection(String a) {

        int x = 0;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            conn = DriverManager.getConnection("jdbc:ucanaccess:" + dbPath);                        
            try (PreparedStatement stmt = conn.prepareStatement("select count(*) as count from " + table + a)) {
                ResultSet rs = stmt.executeQuery();
                rs.next();
                x = Integer.parseInt(rs.getString("count"));
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
        return x;
    }
    ResultSet connection() {

        ResultSet x = null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            conn = DriverManager.getConnection("jdbc:ucanaccess:" + dbPath);                        
            try (PreparedStatement stmt = conn.prepareStatement("select * from " + table)) {
                x = stmt.executeQuery();              
                System.out.println("Success");
            }
            conn.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
        return x;
    }
}
