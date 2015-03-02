package id3;

import java.sql.*;
import java.util.*;
import java.util.logging.*;

public class ID3 {

    String dbPath, table, play;
    Connection conn;
    Tree<String>[] col;
    Tree<String> root;

    ID3(String dbPath, String table, String play) {
        this.dbPath = dbPath;
        this.table = table;
        this.play = play;
    }

    public void colls() {
        try {
            
            ResultSet rs = connection("");
            ResultSetMetaData rm = rs.getMetaData();
            col = new Tree[rm.getColumnCount() - 2];
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

    public Tree<String> start() {
        colls();
        int a = run(null);
        root = new Tree<>(col[a].data);
        ArrayList<Tree<String>> alt = new ArrayList<>();
        for (int i = 0; i < col[a].children.size(); i++) {
            root.addChild(col[a].children.get(i).data);
            alt.add(root.children.get(i));
        }
        int k = 0;
        while ((a = run(alt.get(k))) != -1) {

            if (a == -2) {
                alt.get(k).addChild("yes");
                if (++k == alt.size())
                    return root;
                continue;
            } else if (a == -3) {
                alt.get(k).addChild("no");
                if (++k == alt.size())
                    return root;
                continue;
            }
            Tree<String> temp = alt.get(k).addChild(col[a].data);
            for (int i = 0; i < col[a].children.size(); i++) {

                Tree<String> temp1 = temp.addChild(col[a].children.get(i).data);
                alt.add(temp1);

            }
            if (++k == alt.size())
                return root;
        }

        return root;

    }

    static String where(Tree<String> n) {

        String x = " where ";
        while (true) {
            String a = n.parent.data;
            String b = n.data;
            x += a + "='" + b + "'";
            if (n.parent.parent == null)
                return x;
            x += " and ";
            n = n.parent.parent;
        }
    }

    int run(Tree<String> wh) {

        try {
            ResultSet rs;
            String x;
            if (wh == null) {
                rs = connection("");
                x = " where ";
            } else {
                x = where(wh);
                rs = connection(x);
                x += " and ";
            }
            String[] abc = x.split(" ");
            if (abc.length > col.length*2)
                return -1;
            double t = 0;
            while (rs.next())
                t++;
            rs.close();
            double gain = Double.POSITIVE_INFINITY;
            int count = 10;
            int n = 0, y = 0, g = 0;
            for (int i = 0; i < col.length; i++) {

                if (!x.contains(col[i].data)) {
                    double o = 0, h;
                    for (int j = 0; j < col[i].children.size(); j++) {
                        g++;
                        h = calc(x + col[i].data + " = '" + col[i].children.get(j).data + "'", t);

                        if (h == 0) {
                            y++;
                            continue;
                        } else if (h == 1) {
                            n++;
                            continue;
                        } else if (h == 2) {
                            n++;
                            y++;
                        }
                        o += h;

                    }

                    if (gain > Math.abs(o)) {
                        gain = o;
                        count = i;
                    }

                }
            }            
            if (y == g)
                return -2;
            else if (n == g)
                return -3;
            return count;
        } catch (SQLException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    double calc(String a, double t) {

        double y = 0, n = 0;
        try (ResultSet rs = connection(a)) {
            while (rs.next())
                if (rs.getString(play).equals("yes"))
                    y++;
                else
                    n++;
        } catch (SQLException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (y == 0 && n == 0) 
            return 2;
        else if (y == 0) 
            
            return 1;
         else if (n == 0) 
            
            return 0;
        

        double gain = -(y / (y + n) * Math.log(y / (y + n)) / Math.log(2)) - (n / (y + n) * Math.log(n / (y + n)) / Math.log(2));
        return ((y + n) / t) * gain;

    }

    ResultSet connection(String a) {

        ResultSet rs=null;
        try {
            Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            conn = DriverManager.getConnection("jdbc:ucanaccess:" + dbPath);
            PreparedStatement stmt = conn.prepareStatement("select * from " + table + a);
            rs =  stmt.executeQuery();
            conn.close();
            stmt.close();
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(ID3.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rs;
    }
}
