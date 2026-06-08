package LMS;

import java.util.Date;

public class HoldRequest {
    
    private Borrower borrower;
    private Book book;
    private Date requestDate;
    
    public HoldRequest(Borrower bor, Book b, Date reqDate)  // 构造方法
    {
        borrower = bor;
        book = b;
        requestDate = reqDate;
    }
    
    /*----- Getter FUNCs.--------*/
    public Borrower getBorrower() { return borrower; }

    public Book getBook()
    {
        return book;
    }
    
    public Date getRequestDate()
    {
        return requestDate;
    }
    /*--------------------------*/
    
    // 打印预约数据
    public void print()
    {
        System.out.println(book.getTitle() + "\t\t\t\t" + borrower.getName() + "\t\t\t\t"  + requestDate );
    }
}// HoldRequest Class Closed
