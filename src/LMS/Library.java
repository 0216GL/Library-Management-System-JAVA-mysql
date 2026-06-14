package LMS;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Library {

    private String name;                                // 图书馆名称
    public static Librarian librarian;                  // 馆长对象（唯一）
    public static ArrayList <Person> persons;           // 所有人员（职员和借阅者）
    private final ArrayList <Book> booksInLibrary;            // 馆藏所有图书

    private final ArrayList <Loan> loans;                     // 所有借阅历史记录

    public int book_return_deadline;                    // 归还期限（超过此期限将产生罚款）
    public double per_day_fine;                         // 每日罚款金额

    public int hold_request_expiry;                     // 预约有效期（天数）

    /*----采用单例设计模式（懒汉式）------------*/
    private static Library obj;

    public static Library getInstance(){
        if(obj==null) {
            obj = new Library();
        }
        return obj;
    }
    /*---------------------------------------------------------------------*/

    private Library()   // 默认构造函数
    {
        name = null;
        librarian = null;

        persons = new ArrayList<>();

        booksInLibrary = new ArrayList<>();
        loans = new ArrayList<>();
    }


    /*------------Setter 方法------------*/

    public void setReturnDeadline(int deadline)
    {
        book_return_deadline = deadline;
    }

    public void setFine(double perDayFine)
    {
        per_day_fine = perDayFine;
    }

    public void setRequestExpiry(int hrExpiry)
    {
        hold_request_expiry = hrExpiry;
    }
    /*--------------------------------------*/




    // 设置图书馆名称
    public void setName(String n)
    {
        name = n;
    }

    /*-----------Getter 方法------------*/

    public int getHoldRequestExpiry()
    {
        return hold_request_expiry;
    }

    public ArrayList<Person> getPersons()
    {
        return persons;
    }

    public Librarian getLibrarian()
    {
        return librarian;
    }

    public String getLibraryName()
    {
        return name;
    }

    public ArrayList<Book> getBooks()
    {
        return booksInLibrary;
    }

    /*---------------------------------------*/

    /*-----添加人员到图书馆----*/

    public void addClerk(Clerk c)
    {
        persons.add(c);
    }

    public void addBorrower(Borrower b)
    {
        persons.add(b);
    }


    public void addLoan(Loan l)
    {
        loans.add(l);
    }

    /*----------------------------------------------*/

    /*-----------在图书馆中查找人员--------------*/
    
    /**
     * 根据ID查找借阅者
     */
    public Borrower findBorrower()
    {
        System.out.println("\n请输入借阅者ID: ");

        int id = 0;

        Scanner scanner = new Scanner(System.in);

        try{
            id = scanner.nextInt();
        }
        catch (java.util.InputMismatchException e)
        {
            System.out.println("\n输入无效");
        }

        for (Person person : persons) {
            if (person.getID() == id && person.getClass().getSimpleName().equals("Borrower"))
                return (Borrower) person;
        }

        System.out.println("\n抱歉，未找到匹配的借阅者ID。");
        return null;
    }

    /**
     * 根据ID查找职员
     */
    public Clerk findClerk()
    {
        System.out.println("\n请输入职员ID: ");

        int id = 0;

        Scanner scanner = new Scanner(System.in);

        try{
            id = scanner.nextInt();
        }
        catch (java.util.InputMismatchException e)
        {
            System.out.println("\n输入无效");
        }

        for (Person person : persons) {
            if (person.getID() == id && person.getClass().getSimpleName().equals("Clerk"))
                return (Clerk) person;
        }

        System.out.println("\n抱歉，未找到匹配的职员ID。");
        return null;
    }

    /*------- 图书管理操作--------------*/
    
    /**
     * 添加图书到图书馆
     */
    public void addBookinLibrary(Book b)
    {
        booksInLibrary.add(b);
    }

    public void removeBookfromLibrary(Book b)
    {
        boolean delete = true;

        // 检查该书是否正被某人借阅
        for (int i = 0; i < persons.size() && delete; i++)
        {
            if (persons.get(i).getClass().getSimpleName().equals("Borrower"))
            {
                ArrayList<Loan> borBooks = ((Borrower)(persons.get(i))).getBorrowedBooks();

                for (int j = 0; j < borBooks.size() && delete; j++)
                {
                    if (borBooks.get(j).getBook() == b)
                    {
                        delete = false;
                        System.out.println("这本书当前正被某位借阅者借走。");
                    }
                }
            }
        }

        if (delete)
        {
            System.out.println("\n当前无人借阅此书。");
            ArrayList<HoldRequest> hRequests = b.getHoldRequests();

            if(!hRequests.isEmpty())
            {
                System.out.println("\n此书可能有借阅者的预约请求。删除图书将同时删除相关预约。");
                System.out.println("确定要删除此书吗？(y/n)");

                Scanner sc = new Scanner(System.in);

                while (true)
                {
                    String choice = sc.next();

                    if(choice.equals("y") || choice.equals("n"))
                    {
                        if(choice.equals("n"))
                        {
                            System.out.println("\n删除失败。");
                            return;
                        }
                        else
                        {
                            // 清空图书的预约列表
                            // 同时从借阅者处删除预约
                            for (int i = 0; i < hRequests.size() && delete; i++)
                            {
                                HoldRequest hr = hRequests.get(i);
                                hr.getBorrower().removeHoldRequest(hr);
                            }
                        }
                    }
                    else
                        System.out.println("输入无效，请输入 (y/n): ");
                }

            }
            else
                System.out.println("此书没有预约请求。");

            booksInLibrary.remove(b);
            System.out.println("图书已成功删除。");
        }
        else
            System.out.println("\n删除失败。");
    }



    /**
     * 按书名、主题或作者搜索图书
     */
    public ArrayList<Book> searchForBooks() throws IOException
    {
        String choice;
        String title = "", subject = "", author = "";

        Scanner sc = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true)
        {
            System.out.println("\n请输入 '1' 或 '2' 或 '3' 分别按书名、主题或作者搜索: ");
            choice = sc.next();

            if (choice.equals("1") || choice.equals("2") || choice.equals("3"))
                break;
            else
                System.out.println("\n输入错误！");
        }

        if (choice.equals("1"))
        {
            System.out.println("\n请输入书名: ");
            title = reader.readLine();
        }

        else if (choice.equals("2"))
        {
            System.out.println("\n请输入主题: ");
            subject = reader.readLine();
        }

        else
        {
            System.out.println("\n请输入作者: ");
            author = reader.readLine();
        }

        ArrayList<Book> matchedBooks = new ArrayList<>();

        // 检索所有匹配用户搜索条件的图书
        for (Book b : booksInLibrary) {
            if (choice.equals("1")) {
                if (b.getTitle().equals(title))
                    matchedBooks.add(b);
            } else if (choice.equals("2")) {
                if (b.getSubject().equals(subject))
                    matchedBooks.add(b);
            } else {
                if (b.getAuthor().equals(author))
                    matchedBooks.add(b);
            }
        }

        // 打印所有匹配的图书
        if (!matchedBooks.isEmpty())
        {
            System.out.println("\n找到以下图书: \n");

            System.out.println("------------------------------------------------------------------------------");
            System.out.println("序号\t\t书名\t\t\t作者\t\t\t主题");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < matchedBooks.size(); i++)
            {
                System.out.print(i + "-" + "\t\t");
                matchedBooks.get(i).printInfo();
                System.out.print("\n");
            }

            return matchedBooks;
        }
        else
        {
            System.out.println("\n抱歉，未找到相关图书。");
            return null;
        }
    }



    /**
     * 查看图书馆所有图书信息
     */
     public void viewAllBooks()
    {
        if (!booksInLibrary.isEmpty())
        {
            System.out.println("\n图书列表: ");

            System.out.println("------------------------------------------------------------------------------");
            System.out.println("序号\t\t书名\t\t\t作者\t\t\t主题");
            System.out.println("------------------------------------------------------------------------------");

            for (int i = 0; i < booksInLibrary.size(); i++)
            {
                System.out.print(i + "-" + "\t\t");
                booksInLibrary.get(i).printInfo();
                System.out.print("\n");
            }
        }
        else
            System.out.println("\n当前图书馆没有藏书。");
    }


    /**
     * 计算某借阅者所有借阅记录的总罚款
     */
    public double computeFine2(Borrower borrower)
    {
        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------------------");
        System.out.println("序号\t\t书名\t\t\t借阅者\t\t\t借出日期\t\t\t归还日期\t\t\t罚款(元)");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        double totalFine = 0;
        double per_loan_fine = 0;

        for (int i = 0; i < loans.size(); i++)
        {
            Loan l = loans.get(i);

            if ((l.getBorrower() == borrower))
            {
                per_loan_fine = l.computeFine1();
                System.out.print(i + "-" + "\t\t" + loans.get(i).getBook().getTitle() + "\t\t\t" + loans.get(i).getBorrower().getName() + "\t\t" + loans.get(i).getIssuedDate() +  "\t\t\t" + loans.get(i).getReturnDate() + "\t\t\t\t" + per_loan_fine  + "\n");

                totalFine += per_loan_fine;
            }
        }

        return totalFine;
    }


    /**
     * 创建人员（借阅者/职员/馆长）
     */
    public void createPerson(char x)
    {
        Scanner sc = new Scanner(System.in);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("\n请输入姓名: ");
        String n = "";
        try {
            n = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("请输入地址: ");
        String address = "";
        try {
            address = reader.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }

        String phone = "";

        try{
            System.out.println("请输入电话号码: ");
            phone = sc.next();
        }
        catch (java.util.InputMismatchException e)
        {
            System.out.println("\n输入无效。");
        }

        // 创建职员
        if (x == 'c')
        {
            double salary = 0;

            try{
                System.out.println("请输入工资: ");
                salary = sc.nextDouble();
            }
            catch (java.util.InputMismatchException e)
            {
                System.out.println("\n输入无效。");
            }

            Clerk c = new Clerk(-1,n,address,phone,salary,-1);
            addClerk(c);

            System.out.println("\n职员 " + n + " 创建成功。");
            System.out.println("\n您的ID是: " + c.getID());
            System.out.println("您的密码是: " + c.getPassword());
        }

        // 创建馆长
        else if (x == 'l')
        {
            double salary = 0;
            try{
                System.out.println("请输入工资: ");
                salary = sc.nextDouble();
            }
            catch (java.util.InputMismatchException e)
            {
                System.out.println("\n输入无效。");
            }

            Librarian l = new Librarian(-1,n,address,phone,salary,-1);
            if(Librarian.addLibrarian(l))
            {
                System.out.println("\n馆长 " + n + " 创建成功。");
                System.out.println("\n您的ID是: " + l.getID());
                System.out.println("您的密码是: " + l.getPassword());
            }
        }

        // 创建借阅者
        else
        {
            Borrower b = new Borrower(-1,n,address,phone);
            addBorrower(b);
            System.out.println("\n借阅者 " + n + " 创建成功。");

            System.out.println("\n您的ID是: " + b.getID());
            System.out.println("您的密码是: " + b.getPassword());
        }
    }



    /**
     * 创建图书
     */
    public void createBook(String title, String subject, String author)
    {
        // ✅ 已修复：使用String.valueOf将-1转换为字符串作为ISBN
        Book b = new Book(String.valueOf(-1),title,subject,author,false);

        addBookinLibrary(b);

        System.out.println("\n图书《" + b.getTitle() + "》创建成功。");
    }



    /**
     * 登录验证
     */
    public Person login()
    {
        Scanner input = new Scanner(System.in);

        int id = 0;
        String password = "";
        System.out.println("\n输入 ID: ");
        try{
            id = input.nextInt();
        }
        catch (java.util.InputMismatchException e)
        {
            System.out.println("\n输入无效");
        }

        System.out.println("输入密码: ");
        password = input.next();

        for (Person person : persons) {
            if (person.getID() == id && person.getPassword().equals(password)) {
                System.out.println("\n登录成功");
                return person;
            }
        }

        if(librarian!=null)
        {
            if (librarian.getID() == id && librarian.getPassword().equals(password))
            {
                System.out.println("\n登录成功");
                return librarian;
            }
        }

        System.out.println("\n抱歉！ID或密码错误");
        return null;
    }


    /**
     * 查看借阅历史
     */
    public void viewHistory()
    {
        if (!loans.isEmpty())
        {
            System.out.println("\n借阅记录: ");

            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.println("序号\t书名\t\t借阅者\t\t  经办人\t\t借出日期\t\t\t接收人\t\t归还日期\t\t罚款已缴");
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------");

            for (int i = 0; i < loans.size(); i++)
            {
                if(loans.get(i).getIssuer()!=null)
                    System.out.print(i + "-" + "\t" + loans.get(i).getBook().getTitle() + "\t\t\t" + loans.get(i).getBorrower().getName() + "\t\t" + loans.get(i).getIssuer().getName() + "\t    " + loans.get(i).getIssuedDate());

                if (loans.get(i).getReceiver() != null)
                {
                    System.out.print("\t" + loans.get(i).getReceiver().getName() + "\t\t" + loans.get(i).getReturnDate() +"\t   " + loans.get(i).getFineStatus() + "\n");
                }
                else
                    System.out.print("\t\t" + "--" + "\t\t\t" + "--" + "\t\t" + "--" + "\n");
            }
        }
        else
            System.out.println("\n无借阅记录。");
    }

    //---------------------------------------------------------------------------------------//
    /*--------------------------------与数据库协作------------------------------------------*/

    /**
     * 建立数据库连接
     */
    public Connection makeConnection()
    {
        try
        {
            String host = "jdbc:mysql://localhost:3306/library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            String uName = "root";
            String uPass = "123456";
            Connection con = DriverManager.getConnection( host, uName, uPass );
            return con;
        }
        catch ( SQLException err )
        {
            err.printStackTrace();  // 打印完整的错误堆栈
            System.out.println("SQL 错误: " + err.getMessage());
            return null;
        }
    }
    
    /**
     * 从数据库加载所有数据到内存
     */
    public void populateLibrary(Connection con) throws SQLException, IOException
    {
            Library lib = this;
            Statement stmt = con.createStatement( );

            /* --- 加载图书数据 ----*/
            String SQL = "SELECT * FROM BOOK";
            ResultSet rs = stmt.executeQuery( SQL );

            if(!rs.next())
            {
               System.out.println("\n图书馆中没有图书");
            }
            else
            {
                do
                {
                    if(rs.getString("TITLE") !=null && rs.getString("AUTHOR")!=null && rs.getString("SUBJECT")!=null && rs.getString("ISBN")!=null)
                    {
                        String title=rs.getString("TITLE");
                        String author=rs.getString("AUTHOR");
                        String subject=rs.getString("SUBJECT");
                        String isbn=rs.getString("ISBN");
                        boolean issue=rs.getBoolean("IS_ISSUED");
                        Book b = new Book(isbn,title,subject,author,issue);
                        addBookinLibrary(b);
                    }
                }while(rs.next());
            }

            /* ----加载职员数据----*/

            SQL="SELECT ID,PNAME,ADDRESS,PASSWORD,PHONE_NO,SALARY,DESK_NO FROM PERSON INNER JOIN CLERK ON ID=C_ID INNER JOIN STAFF ON S_ID=C_ID";

            rs=stmt.executeQuery(SQL);

            if(!rs.next())
            {
               System.out.println("图书馆中没有职员");
            }
            else
            {
                do
                {
                    int id=rs.getInt("ID");
                    String cname=rs.getString("PNAME");
                    String adrs=rs.getString("ADDRESS");
                    String phn=rs.getString("PHONE_NO");
                    double sal=rs.getDouble("SALARY");
                    int desk=rs.getInt("DESK_NO");
                    Clerk c = new Clerk(id,cname,adrs,phn,sal,desk);

                    addClerk(c);
                }
                while(rs.next());

            }

            /*-----加载馆长数据---*/
            SQL="SELECT ID,PNAME,ADDRESS,PASSWORD,PHONE_NO,SALARY,OFFICE_NO FROM PERSON INNER JOIN LIBRARIAN ON ID=L_ID INNER JOIN STAFF ON S_ID=L_ID";

            rs=stmt.executeQuery(SQL);
            if(!rs.next())
            {
               System.out.println("图书馆中没有馆长");
            }
            else
            {
                do
                {
                    int id=rs.getInt("ID");
                    String lname=rs.getString("PNAME");
                    String adrs=rs.getString("ADDRESS");
                    String phn=rs.getString("PHONE_NO");
                    double sal=rs.getDouble("SALARY");
                    int off=rs.getInt("OFFICE_NO");
                    Librarian l= new Librarian(id,lname,adrs,phn,sal,off);

                    Librarian.addLibrarian(l);

                }while(rs.next());

            }

            /*---加载借阅者数据（部分）--------*/

            SQL="SELECT ID,PNAME,ADDRESS,PASSWORD,PHONE_NO FROM PERSON INNER JOIN BORROWER ON ID=B_ID";

            rs=stmt.executeQuery(SQL);

            if(!rs.next())
            {
               System.out.println("图书馆中没有借阅者");
            }
            else
            {
                do
                {
                        int id=rs.getInt("ID");
                        String name=rs.getString("PNAME");
                        String adrs=rs.getString("ADDRESS");
                        String phn=rs.getString("PHONE_NO");

                        Borrower b= new Borrower(id,name,adrs,phn);
                        addBorrower(b);

                }while(rs.next());

            }

            /*----加载借阅记录----*/

            SQL="SELECT * FROM LOAN";

            rs=stmt.executeQuery(SQL);
            if(!rs.next())
            {
               System.out.println("暂无图书被借出！");
            }
            else
            {
                do
                    {
                        int borid=rs.getInt("BORROWER");
                        String bookIsbn=rs.getString("BOOK");
                        int iid=rs.getInt("ISSUER");
                        Integer rid=(Integer)rs.getObject("RECEIVER");
                        int rd=0;
                        Date rdate;

                        Date idate=new Date (rs.getTimestamp("ISS_DATE").getTime());

                        if(rid!=null)    // 如果有接收人
                        {
                            rdate=new Date (rs.getTimestamp("RET_DATE").getTime());
                            rd=(int)rid;
                        }
                        else
                        {
                            rdate=null;
                        }

                        boolean fineStatus = rs.getBoolean("FINE_PAID");

                        boolean set=true;

                        Borrower bb = null;


                        for(int i=0;i<getPersons().size() && set;i++)
                        {
                            if(getPersons().get(i).getID()==borid)
                            {
                                set=false;
                                bb=(Borrower)(getPersons().get(i));
                            }
                        }

                        set =true;
                        Staff s[]=new Staff[2];

                        if(iid==getLibrarian().getID())
                        {
                            s[0]=getLibrarian();
                        }

                        else
                        {
                            for(int k=0;k<getPersons().size() && set;k++)
                            {
                                if(getPersons().get(k).getID()==iid && getPersons().get(k).getClass().getSimpleName().equals("Clerk"))
                                {
                                    set=false;
                                    s[0]=(Clerk)(getPersons().get(k));
                                }
                            }
                        }

                        set=true;
                        // 如果尚未归还...
                        if(rid==null)
                        {
                            s[1]=null;  // 无接收人
                            rdate=null;
                        }
                        else
                        {
                            if(rd==getLibrarian().getID())
                                s[1]=getLibrarian();

                            else
                            {    //system.out.println("ff");
                                 for(int k=0;k<getPersons().size() && set;k++)
                                {
                                    if(getPersons().get(k).getID()==rd && getPersons().get(k).getClass().getSimpleName().equals("Clerk"))
                                    {
                                        set=false;
                                        s[1]=(Clerk)(getPersons().get(k));
                                    }
                                }
                            }
                        }

                        set=true;

                        ArrayList<Book> books = getBooks();

                        for(int k=0;k<books.size() && set;k++)
                        {
                            if(books.get(k).getISBN().equals(bookIsbn))
                            {
                              set=false;
                              Loan l = new Loan(bb,books.get(k),s[0],s[1],idate,rdate,fineStatus);
                              loans.add(l);
                            }
                        }

                    }while(rs.next());
            }

            /*----加载预约图书记录----*/

            SQL="SELECT * FROM ON_HOLD_BOOK";

            rs=stmt.executeQuery(SQL);
            if(!rs.next())
            {
               System.out.println("暂无预约图书！");
            }
            else
            {
                do
                    {
                        int borid =rs.getInt("BORROWER");
                        String bookIsbn=rs.getString("BOOK");
                        Date off=new Date (rs.getDate("REQ_DATE").getTime());

                        boolean set=true;
                        Borrower bb =null;

                        ArrayList<Person> persons = lib.getPersons();

                        for(int i=0;i<persons.size() && set;i++)
                        {
                            if(persons.get(i).getID()== borid)
                            {
                                set=false;
                                bb=(Borrower)(persons.get(i));
                            }
                        }

                        set=true;

                        ArrayList<Book> books = lib.getBooks();

                        for(int i=0;i<books.size() && set;i++)
                        {
                            if(books.get(i).getISBN().equals(bookIsbn))
                            {
                              set=false;
                              books.get(i).placeBookOnHold(bb);
                            }
                        }
                        }while(rs.next());
            }

            /* --- 加载借阅者的剩余信息----*/

            // 已借图书
            SQL="SELECT ID,BOOK FROM PERSON INNER JOIN BORROWER ON ID=B_ID INNER JOIN BORROWED_BOOK ON B_ID=BORROWER ";

            rs=stmt.executeQuery(SQL);

            if(!rs.next())
            {
               System.out.println("暂无借阅者借书");
            }
            else
            {

                do
                    {
                        int id=rs.getInt("ID");      // 借阅者
                        String bookIsbn=rs.getString("BOOK");   // 图书

                        Borrower bb=null;
                        boolean set=true;
                        boolean okay=true;

                        for(int i=0;i<lib.getPersons().size() && set;i++)
                        {
                            if(lib.getPersons().get(i).getClass().getSimpleName().equals("Borrower"))
                            {
                                if(lib.getPersons().get(i).getID()==id)
                                {
                                   set =false;
                                    bb=(Borrower)(lib.getPersons().get(i));
                                }
                            }
                        }

                        set=true;

                        ArrayList<Loan> books = loans;

                        for(int i=0;i<books.size() && set;i++)
                        {
                            Loan existingLoan = books.get(i);
                            
                            if (existingLoan != null 
                                && existingLoan.getBook() != null 
                                && existingLoan.getBook().getISBN().equals(bookIsbn)
                                && existingLoan.getReceiver() == null)
                            {
                                set = false;
                                Loan newLoan = new Loan(
                                    bb,
                                    existingLoan.getBook(),
                                    existingLoan.getIssuer(),
                                    null,
                                    existingLoan.getIssuedDate(),
                                    null,
                                    existingLoan.getFineStatus()
                                );
                                bb.addBorrowedBook(newLoan);
                                break;
                            }
                        }

                    }while(rs.next());
            }

            ArrayList<Person> persons = lib.getPersons();

            /* 设置人员ID计数器 */
            int max=0;

            for(int i=0;i<persons.size();i++)
            {
                if (max < persons.get(i).getID())
                    max=persons.get(i).getID();
            }

            Person.setIDCount(max);
    }


    /**
     * 将更改保存回数据库
     */
    public void fillItBack(Connection con) throws SQLException,SQLIntegrityConstraintViolationException
    {
            /*-----------清空借阅记录表------------*/

            String template = "DELETE FROM LIBRARY.LOAN";
            PreparedStatement stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空已借图书表------------*/

            template = "DELETE FROM LIBRARY.BORROWED_BOOK";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空预约图书记录表------------*/

            template = "DELETE FROM LIBRARY.ON_HOLD_BOOK";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空图书表------------*/

            template = "DELETE FROM LIBRARY.BOOK";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空职员表------------*/

            template = "DELETE FROM LIBRARY.CLERK";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空馆长表------------*/

            template = "DELETE FROM LIBRARY.LIBRARIAN";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空借阅者表------------*/

            template = "DELETE FROM LIBRARY.BORROWER";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空职员总表------------*/

            template = "DELETE FROM LIBRARY.STAFF";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            /*-----------清空人员表------------*/

            template = "DELETE FROM LIBRARY.PERSON";
            stmts = con.prepareStatement(template);

            stmts.executeUpdate();

            Library lib = this;

        /* 填充人员表*/
        for(int i=0;i<lib.getPersons().size();i++)
        {
            template = "INSERT INTO LIBRARY.PERSON (ID,PNAME,PASSWORD,ADDRESS,PHONE_NO) values (?,?,?,?,?)";
            PreparedStatement stmt = con.prepareStatement(template);

            stmt.setInt(1, lib.getPersons().get(i).getID());
            stmt.setString(2, lib.getPersons().get(i).getName());
            stmt.setString(3,  lib.getPersons().get(i).getPassword());
            stmt.setString(4, lib.getPersons().get(i).getAddress());
            stmt.setString(5, lib.getPersons().get(i).getPhoneNumber());

            stmt.executeUpdate();
        }

        /* 填充职员表和职员总表*/
        for(int i=0;i<lib.getPersons().size();i++)
        {
            if (lib.getPersons().get(i).getClass().getSimpleName().equals("Clerk"))
            {
                template = "INSERT INTO LIBRARY.STAFF (S_ID,TYPE,SALARY) values (?,?,?)";
                PreparedStatement stmt = con.prepareStatement(template);

                stmt.setInt(1,lib.getPersons().get(i).getID());
                stmt.setString(2,"Clerk");
                stmt.setDouble(3, ((Clerk)(lib.getPersons().get(i))).getSalary());

                stmt.executeUpdate();

                template = "INSERT INTO LIBRARY.CLERK (C_ID,DESK_NO) values (?,?)";
                stmt = con.prepareStatement(template);

                stmt.setInt(1,lib.getPersons().get(i).getID());
                stmt.setInt(2, ((Clerk)(lib.getPersons().get(i))).deskNo);

                stmt.executeUpdate();
            }

        }

        if(lib.getLibrarian()!=null)    // 如果有馆长
            {
            template = "INSERT INTO LIBRARY.STAFF (S_ID,TYPE,SALARY) values (?,?,?)";
            PreparedStatement stmt = con.prepareStatement(template);

            stmt.setInt(1, lib.getLibrarian().getID());
            stmt.setString(2,"Librarian");
            stmt.setDouble(3,lib.getLibrarian().getSalary());

            stmt.executeUpdate();

            template = "INSERT INTO LIBRARY.LIBRARIAN (L_ID,OFFICE_NO) values (?,?)";
            stmt = con.prepareStatement(template);

            stmt.setInt(1,lib.getLibrarian().getID());
            stmt.setInt(2, lib.getLibrarian().officeNo);

            stmt.executeUpdate();
            }

        /* 填充借阅者表*/
        for(int i=0;i<lib.getPersons().size();i++)
        {
            if (lib.getPersons().get(i).getClass().getSimpleName().equals("Borrower"))
            {
                template = "INSERT INTO LIBRARY.BORROWER(B_ID) values (?)";
                PreparedStatement stmt = con.prepareStatement(template);

                stmt.setInt(1, lib.getPersons().get(i).getID());

                stmt.executeUpdate();
            }
        }

        ArrayList<Book> books = lib.getBooks();

        /*填充图书表*/
        for(int i=0;i<books.size();i++)
        {
            template = "INSERT INTO LIBRARY.BOOK (ID,TITLE,AUTHOR,SUBJECT,IS_ISSUED) values (?,?,?,?,?)";
            PreparedStatement stmt = con.prepareStatement(template);

            stmt.setString(1,books.get(i).getISBN());
            stmt.setString(2,books.get(i).getTitle());
            stmt.setString(3, books.get(i).getAuthor());
            stmt.setString(4, books.get(i).getSubject());
            stmt.setBoolean(5, books.get(i).getIssuedStatus());
            stmt.executeUpdate();

        }

        /* 填充借阅记录表*/
        for(int i=0;i<loans.size();i++)
        {
            template = "INSERT INTO LIBRARY.LOAN(L_ID,BORROWER,BOOK,ISSUER,ISS_DATE,RECEIVER,RET_DATE,FINE_PAID) values (?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = con.prepareStatement(template);

            stmt.setInt(1,i+1);
            stmt.setInt(2,loans.get(i).getBorrower().getID());
            stmt.setString(3,loans.get(i).getBook().getISBN());
            stmt.setInt(4,loans.get(i).getIssuer().getID());
            stmt.setTimestamp(5,new java.sql.Timestamp(loans.get(i).getIssuedDate().getTime()));
            stmt.setBoolean(8,loans.get(i).getFineStatus());
            if(loans.get(i).getReceiver()==null)
            {
                stmt.setNull(6,Types.INTEGER);
                stmt.setDate(7,null);
            }
            else
            {
                stmt.setInt(6,loans.get(i).getReceiver().getID());
                stmt.setTimestamp(7,new java.sql.Timestamp(loans.get(i).getReturnDate().getTime()));
            }

            stmt.executeUpdate();

        }

        /* 填充预约图书记录表*/

        int x=1;
        for(int i=0;i<lib.getBooks().size();i++)
        {
            for(int j=0;j<lib.getBooks().get(i).getHoldRequests().size();j++)
            {
            template = "INSERT INTO LIBRARY.ON_HOLD_BOOK(REQ_ID,BOOK,BORROWER,REQ_DATE) values (?,?,?,?)";
            PreparedStatement stmt = con.prepareStatement(template);

            stmt.setInt(1,x);
            stmt.setInt(3,lib.getBooks().get(i).getHoldRequests().get(j).getBorrower().getID());
            stmt.setString(2,lib.getBooks().get(i).getHoldRequests().get(j).getBook().getISBN());
            stmt.setDate(4,new java.sql.Date(lib.getBooks().get(i).getHoldRequests().get(j).getRequestDate().getTime()));

            stmt.executeUpdate();
            x++;

            }
        }

        /* 填充已借图书表*/
        for(int i=0;i<lib.getBooks().size();i++)
          {
              if(lib.getBooks().get(i).getIssuedStatus()==true)
              {
                  boolean set=true;
                  for(int j=0;j<loans.size() && set ;j++)
                  {
                      if(Objects.equals(lib.getBooks().get(i).getISBN(), loans.get(j).getBook().getISBN()))
                      {
                          if(loans.get(j).getReceiver()==null)
                          {
                            template = "INSERT INTO LIBRARY.BORROWED_BOOK(BOOK,BORROWER) values (?,?)";
                            PreparedStatement stmt = con.prepareStatement(template);
                            stmt.setString(1,loans.get(j).getBook().getISBN());
                            stmt.setInt(2,loans.get(j).getBorrower().getID());

                            stmt.executeUpdate();
                            set=false;
                          }
                      }

                  }

              }
          }
    } // 填充完成！





}   // Library类结束
