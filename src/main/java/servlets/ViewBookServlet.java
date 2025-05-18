package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

public class ViewBookServlet extends HttpServlet {

    // book service for database operations and logics
    BookService bookService = new BookServiceImpl();

    // Mapping from barcode (ISBN) to Amazon cover image URL
    private static final Map<String, String> BOOK_IMAGE_URLS = new HashMap<>();
    static {
        BOOK_IMAGE_URLS.put("9780765365279", "https://m.media-amazon.com/images/I/51hAwcG3oNL._SY445_SX342_QL70_FMwebp_.jpg"); // The Way of Kings
        BOOK_IMAGE_URLS.put("9781250318541", "https://m.media-amazon.com/images/I/61rYqiz8yJL._SY445_SX342_QL70_FMwebp_.jpg"); // Mistborn: The Final Empire
        BOOK_IMAGE_URLS.put("9780593640340", "https://m.media-amazon.com/images/I/71-1WBgjGoL._SX342_.jpg"); // Dune
        BOOK_IMAGE_URLS.put("9780451524935", "https://m.media-amazon.com/images/I/71kxa1-0mfL._AC_UF1000,1000_QL80_.jpg"); // 1984
        BOOK_IMAGE_URLS.put("9780451526342", "https://m.media-amazon.com/images/I/51eg2A6UpdL._SY445_SX342_QL70_FMwebp_.jpg"); // Animal Farm
        BOOK_IMAGE_URLS.put("9780756404741", "https://m.media-amazon.com/images/I/91OqU1cAmrL._SX342_.jpg"); // The Name of the Wind
        BOOK_IMAGE_URLS.put("9780345391803", "https://m.media-amazon.com/images/I/515JSQhPkAL._SY445_SX342_.jpg"); // The Hitchhiker's Guide to the Galaxy
        BOOK_IMAGE_URLS.put("9780441478125", "https://m.media-amazon.com/images/I/81Ck6doFEDL._SX342_.jpg"); // The Left Hand of Darkness
        BOOK_IMAGE_URLS.put("9780593139158", "https://m.media-amazon.com/images/I/81z6Yc8fi7L._SX342_.jpg"); // Greenlights
        BOOK_IMAGE_URLS.put("9781635575569", "https://m.media-amazon.com/images/I/81JnHhjXMZL._SX342_.jpg"); // A Court of Thorns and Roses
        BOOK_IMAGE_URLS.put("9781649377371", "https://m.media-amazon.com/images/I/61D7uTS7-TL._SY445_SX342_QL70_FMwebp_.jpg"); // Fourth Wing
        BOOK_IMAGE_URLS.put("9780593466490", "https://m.media-amazon.com/images/I/615ZWwoPBRL._SY445_SX342_QL70_FMwebp_.jpg"); // Tomorrow, and Tomorrow, and Tomorrow
    }

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        res.setContentType("text/html");

        // Check if the customer is logged in, or else return to login page
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\"><tr><td>Please Login First to Continue!!</td></tr></table>");
            return;
        }
        try {

            // Read All available books from the database
            List<Book> books = bookService.getAllBooks();

            // Default Page to load data into
            RequestDispatcher rd = req.getRequestDispatcher("CustomerHome.html");
            rd.include(req, res);

            // Set Available Books tab as active
            StoreUtil.setActiveTab(pw, "books");

            // Show the heading for the page
            pw.println("<div id='topmid' style='background-color:grey'>Available Books"
                    + "<form action=\"cart\" method=\"post\" style='float:right; margin-right:20px'>"
                    + "<input type='submit' class=\"btn btn-primary\" name='cart' value='Proceed'/></form>"
                    + "</div>");
            pw.println("<div class=\"container\">\r\n"
                    + "        <div class=\"card-columns\">");

            // Add or Remove items from the cart, if requested
            StoreUtil.updateCartItems(req);

            HttpSession session = req.getSession();
            for (Book book : books) {
                // Add each book to display as a card
                pw.println(this.addBookToCard(session, book));
            }

            // Checkout Button
            pw.println("</div>"
                    + "<div style='float:auto'><form action=\"cart\" method=\"post\">"
                    + "<input type='submit' class=\"btn btn-success\" name='cart' value='Proceed to Checkout'/></form>"
                    + "    </div>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String addBookToCard(HttpSession session, Book book) {
        String bCode = book.getBarcode();
        int bQty = book.getQuantity();

        // Quantity of the current book added to the cart
        int cartItemQty = 0;
        if (session.getAttribute("qty_" + bCode) != null) {
            cartItemQty = (int) session.getAttribute("qty_" + bCode);
        }

        // Button To Add/Remove item from the cart
        String button = "";
        if (bQty > 0) {
            button = "<form action=\"viewbook\" method=\"post\">"
                    + "<input type='hidden' name = 'selectedBookId' value = " + bCode + ">"
                    + "<input type='hidden' name='qty_" + bCode + "' value='1'/>"
                    + (cartItemQty == 0
                            ? "<input type='submit' class=\"btn btn-primary\" name='addToCart' value='Add To Cart'/></form>"
                            : "<form method='post' action='cart'>"
                                    + "<button type='submit' name='removeFromCart' class=\"glyphicon glyphicon-minus btn btn-danger\"></button> "
                                    + "<input type='hidden' name='selectedBookId' value='" + bCode + "'/>"
                                    + cartItemQty
                                    + " <button type='submit' name='addToCart' class=\"glyphicon glyphicon-plus btn btn-success\"></button></form>")
                    + "";
        } else {
            button = "<p class=\"btn btn-danger\">Out Of Stock</p>\r\n";
        }

        // Get Amazon image URL or fallback image
        String imageUrl = BOOK_IMAGE_URLS.getOrDefault(bCode, "https://m.media-amazon.com/images/I/61Iz2yy2CKL._AC_UF1000,1000_QL80_.jpg");

        // Bootstrap card to show the book data
        return "<div class=\"card\">\r\n"
                + "  <div class=\"row card-body\">\r\n"
                + "    <img class=\"col-sm-6\" src=\"" + imageUrl + "\" alt=\"Book cover\" style=\"max-width:100%; height:auto;\">\r\n"
                + "    <div class=\"col-sm-6\">\r\n"
                + "      <h5 class=\"card-title text-success\">" + book.getName() + "</h5>\r\n"
                + "      <p class=\"card-text\">\r\n"
                + "        Author: <span class=\"text-primary\" style=\"font-weight:bold;\">"
                + book.getAuthor()
                + "</span><br>\r\n"
                + "      </p>\r\n"
                + "    </div>\r\n"
                + "  </div>\r\n"
                + "  <div class=\"row card-body\">\r\n"
                + "    <div class=\"col-sm-6\">\r\n"
                + "      <p class=\"card-text\">\r\n"
                + "        <span>Id: " + bCode + "</span>\r\n"
                + (bQty < 20 ? "<br><span class=\"text-danger\">Only " + bQty + " items left</span>\r\n"
                        : "<br><span class=\"text-success\">Trending</span>\r\n")
                + "      </p>\r\n"
                + "    </div>\r\n"
                + "    <div class=\"col-sm-6\">\r\n"
                + "      <p class=\"card-text\">\r\n"
                + "        Price: <span style=\"font-weight:bold; color:pink\"> R "
                + book.getPrice()
                + " </span>\r\n"
                + "      </p>\r\n"
                + button
                + "    </div>\r\n"
                + "  </div>\r\n"
                + "</div>";
    }
}
