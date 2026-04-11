# BÁO CÁO DỰ ÁN — BOOKSTORE WEB APPLICATION

## Thông tin dự án

| | |
|---|---|
| **Công nghệ** | Java 21 · Spring Boot 3.5 · Spring Security · Spring Data JPA · Thymeleaf · MySQL · Maven |
| **URL** | http://localhost:8080 |
| **Database** | `bookstore_db` (MySQL) |

---

## Cách chạy ứng dụng

### Yêu cầu cài đặt
- Java 21+
- MySQL (Laragon / XAMPP / standalone)
- Maven (hoặc dùng mvnw đi kèm dự án)

### Bước 1 — Import database
Mở terminal tại thư mục dự án, chạy:
```bash
# Dùng MySQL của Laragon
C:\laragon\bin\mysql\mysql-8.0.30-winx64\bin\mysql.exe -u root bookstore_db < sql.sql

# Hoặc MySQL thông thường
mysql -u root bookstore_db < sql.sql
```

Nếu database chưa tồn tại, tạo trước:
```sql
CREATE DATABASE bookstore_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2 — Cấu hình kết nối (nếu cần)
Mở file `src/main/resources/application.properties`, kiểm tra:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bookstore_db
spring.datasource.username=root
spring.datasource.password=        # để trống nếu dùng Laragon
```

### Bước 3 — Chạy ứng dụng
```bash
# Dùng Maven Wrapper (khuyến nghị)
.\mvnw.cmd spring-boot:run        # Windows
./mvnw spring-boot:run            # Linux/Mac

# Hoặc nếu đã cài Maven global
mvn spring-boot:run
```

### Bước 4 — Mở trình duyệt
Truy cập: **http://localhost:8080**

---

## Tài khoản đăng nhập

| Vai trò | Username | Password | Email |
|---------|----------|----------|-------|
| **Admin** | `admin` | `admin123` | admin@bookstore.com |
| **User** | `user` | `123456` | user@bookstore.com |

> Hoặc tự đăng ký tài khoản mới tại `/signup`

---

## Danh sách tính năng

### Nhóm 1 — Xác thực người dùng (Auth)
| # | Tính năng | URL |
|---|-----------|-----|
| 1 | Đăng ký tài khoản | `/signup` |
| 2 | Đăng nhập | `/login` |
| 3 | Đăng xuất | `/logout` (POST) |
| 4 | Quên mật khẩu — gửi link qua email | `/forgot-password` |
| 5 | Đặt lại mật khẩu qua link email | `/reset-password?token=...` |

### Nhóm 2 — Danh mục sách (Catalog)
| # | Tính năng | URL |
|---|-----------|-----|
| 6 | Xem danh sách sách có phân trang (5 sách/trang) | `/books` |
| 7 | Tìm kiếm sách theo từ khóa (tên, tác giả) | `/books?keyword=...` |
| 8 | Lọc theo danh mục (Programming / Business / Novel) | `/books?categoryId=...` |
| 9 | Lọc theo khoảng giá | `/books?minPrice=...&maxPrice=...` |
| 10 | Sắp xếp theo giá tăng/giảm | `/books?sort=asc\|desc` |
| 11 | Xem chi tiết sách, đánh giá của người dùng | `/books/{id}` |

### Nhóm 3 — Giỏ hàng (Cart) — Session based
| # | Tính năng | URL |
|---|-----------|-----|
| 12 | Xem giỏ hàng | `/cart` |
| 13 | Thêm sách vào giỏ | POST `/cart/add` |
| 14 | Cập nhật số lượng | POST `/cart/update` |
| 15 | Xóa sản phẩm khỏi giỏ | POST `/cart/remove` |

### Nhóm 4 — Đặt hàng (Order)
| # | Tính năng | URL |
|---|-----------|-----|
| 16 | Chọn sản phẩm từ giỏ để thanh toán | `/orders/checkout` (GET) |
| 17 | Điền thông tin nhận hàng, áp mã giảm giá, đặt hàng | POST `/orders/checkout` |
| 18 | Trang xác nhận đặt hàng thành công | `/orders/success` |
| 19 | Lịch sử đơn hàng (phân trang) | `/orders` |
| 20 | Xem chi tiết đơn hàng & lịch sử trạng thái | `/orders/{id}` |
| 21 | Hủy đơn hàng (trạng thái PENDING) | POST `/orders/{id}/cancel` |
| 22 | Chuyển trạng thái đơn hàng (mock shipping) | POST `/orders/{id}/advance-status` |

### Nhóm 5 — Đánh giá sách (Review)
| # | Tính năng | URL |
|---|-----------|-----|
| 23 | Đánh giá sách (1–5 sao + bình luận) | POST `/reviews/add/{bookId}` |

### Nhóm 6 — Danh sách yêu thích (Wishlist)
| # | Tính năng | URL |
|---|-----------|-----|
| 24 | Xem wishlist cá nhân | `/wishlist` |
| 25 | Thêm sách vào wishlist | POST `/wishlist/add/{bookId}` |
| 26 | Xóa sách khỏi wishlist | POST `/wishlist/remove/{bookId}` |

### Nhóm 7 — Gợi ý sách (Recommendations)
| # | Tính năng | URL |
|---|-----------|-----|
| 27 | Xem sách được gợi ý cá nhân hóa (dựa trên lịch sử đơn + wishlist) | `/recommendations` |

### Nhóm 8 — Hồ sơ người dùng (Profile)
| # | Tính năng | URL |
|---|-----------|-----|
| 28 | Xem thông tin tài khoản | `/profile` |

### Nhóm 9 — Quản trị: Sách (Admin - Book Management)
| # | Tính năng | URL |
|---|-----------|-----|
| 29 | Danh sách tất cả sách + cảnh báo tồn kho thấp (≤5) | `/admin/books` |
| 30 | Thêm sách mới | `/admin/books/create` |
| 31 | Sửa thông tin sách | `/admin/books/edit?id=...` |
| 32 | Xóa sách | POST `/admin/books/delete` |

### Nhóm 10 — Quản trị: Thống kê (Admin - Analytics)
| # | Tính năng | URL |
|---|-----------|-----|
| 33 | Dashboard thống kê: tổng doanh thu, tổng đơn, biểu đồ doanh số theo ngày | `/admin/analytics` |

### Nhóm 11 — Quản trị: Mã giảm giá (Admin - Discount Code)
| # | Tính năng | URL |
|---|-----------|-----|
| 34 | Danh sách mã giảm giá | `/discount-codes/admin` |
| 35 | Tạo mã giảm giá mới (giảm cố định hoặc %, có thời hạn, giới hạn số lần dùng) | `/discount-codes/admin/create` |
| 36 | Xóa mã giảm giá | POST `/discount-codes/admin/delete/{id}` |

---

## Thứ tự trình bày khi báo cáo

### Phần 1 — Giới thiệu & Kiến trúc (3–5 phút)
1. Giới thiệu đề tài: Website bán sách trực tuyến
2. Công nghệ sử dụng: Spring Boot, Spring Security, JPA/Hibernate, Thymeleaf, MySQL
3. Kiến trúc MVC: Controller → Service → Repository → Database
4. Demo khởi chạy ứng dụng (`mvnw spring-boot:run`)

### Phần 2 — Tính năng người dùng thông thường (10–12 phút)
5. **Trang chủ / Danh mục sách** — tìm kiếm, lọc danh mục, lọc giá, sắp xếp, phân trang
6. **Chi tiết sách** — xem mô tả, giá, tồn kho, đánh giá của cộng đồng
7. **Đăng ký tài khoản** — form signup, validation
8. **Đăng nhập / Đăng xuất**
9. **Quên mật khẩu** — nhập email → nhận link → đặt lại mật khẩu → redirect về login
10. **Giỏ hàng** — thêm sách, cập nhật số lượng, xóa, xem tổng tiền
11. **Thanh toán** — chọn sản phẩm, nhập thông tin, áp mã giảm giá, xác nhận
12. **Lịch sử đơn hàng** — xem các đơn, xem chi tiết, hủy đơn, theo dõi trạng thái
13. **Đánh giá sách** — gửi đánh giá 1–5 sao + bình luận
14. **Wishlist** — thêm/xóa sách yêu thích
15. **Gợi ý sách** — recommendations cá nhân hóa
16. **Hồ sơ cá nhân**

### Phần 3 — Tính năng Admin (5–7 phút)
17. **Quản lý sách** — thêm, sửa, xóa sách; cảnh báo tồn kho thấp
18. **Thống kê doanh thu** — tổng đơn hàng, tổng doanh thu, biểu đồ theo ngày
19. **Quản lý mã giảm giá** — tạo, xem, xóa discount code

### Phần 4 — Unit Tests & Chất lượng (3–5 phút)
20. Demo chạy `mvnw test` → **12/12 tests PASS**
21. Nêu các test case: tìm kiếm, phân trang, sắp xếp, lọc, giỏ hàng, checkout, quên mật khẩu, đặt lại mật khẩu, tiến trạng thái đơn hàng, đánh giá sách, tồn kho thấp

---

## Kết quả kiểm thử tự động

```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Case | Mô tả | Kết quả |
|-----------|-------|---------|
| `contextLoads` | Ứng dụng khởi động thành công | ✅ |
| `shouldSearchBooksByKeyword` | Tìm kiếm sách theo từ khóa | ✅ |
| `shouldPaginateFiveBooksPerPage` | Phân trang 5 sách/trang | ✅ |
| `shouldSortBooksByPriceAscendingAndDescending` | Sắp xếp theo giá tăng/giảm | ✅ |
| `shouldFilterBooksByCategory` | Lọc theo danh mục | ✅ |
| `shouldAddBookToSessionCartAndShowTotals` | Thêm vào giỏ & hiển thị tổng tiền | ✅ |
| `shouldCreateOrderDetailsAndTotalWhenCheckout` | Tạo đơn hàng & tính tổng | ✅ |
| `shouldAdvanceOrderStatus` | Chuyển trạng thái đơn hàng | ✅ |
| `shouldSubmitAndDisplayBookReview` | Gửi & hiển thị đánh giá sách | ✅ |
| `shouldShowLowStockWarningInAdminList` | Cảnh báo tồn kho thấp (admin) | ✅ |
| `shouldGeneratePasswordResetTokenWhenForgotPassword` | Tạo token đặt lại mật khẩu | ✅ |
| `shouldResetPasswordWithValidToken` | Đặt lại mật khẩu & redirect về login | ✅ |

---

## Cấu trúc thư mục dự án

```
src/
├── main/
│   ├── java/com/bookstore/
│   │   ├── config/          # DataInitializer, EmailConfig, SecurityConfig
│   │   ├── controller/      # HomeController, BookController, OrderController, ...
│   │   ├── dto/             # CheckoutForm, ForgotPasswordRequest, ...
│   │   ├── model/           # AppUser, Book, BookOrder, DiscountCode, ...
│   │   ├── repository/      # JPA Repositories
│   │   └── service/         # AuthService, OrderService, EmailService, ...
│   └── resources/
│       ├── application.properties
│       ├── templates/       # Thymeleaf HTML templates
│       └── static/          # CSS
├── test/
│   └── java/com/bookstore/
│       └── BookstoreApplicationTests.java   # 12 integration tests
└── sql.sql                  # Database dump
```

---

*Báo cáo được tạo tự động — April 2026*
