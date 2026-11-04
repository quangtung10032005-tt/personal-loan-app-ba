Ứng dụng Vay vốn & Chi tiêu Cá nhân (Android Application)

Dự án Bài tập lớn học phần: Phát triển ứng dụng trên thiết bị di động  

Đơn vị: Trường Công nghệ thông tin và Truyền thông – Đại học Công nghiệp Hà Nội (HaUI)  

Vai trò đảm nhận: Business Analyst (BA) / System Analyst (Phân tích hệ thống & Thiết kế dữ liệu)

1. Tổng quan dự án (Project Overview)
Trong kỷ nguyên số, việc cân bằng giữa thu nhập, chi tiêu và các khoản vay nợ là một thách thức lớn đối với sinh viên và người trẻ đi làm. Ý tưởng về Ứng dụng Vay vốn và Chi tiêu cá nhân ra đời nhằm cung cấp một giải pháp "Local-first" (hoạt động Offline an toàn), tích hợp 2 nghiệp vụ cốt lõi trên cùng một nền tảng:
 + Theo dõi và quản lý dòng tiền Thu/Chi hàng ngày.
 + Quản lý Tín dụng cá nhân & Tính toán lịch trình trả nợ (gốc + lãi).

2. Các luồng Nghiệp vụ cốt lõi (Core Business Modules)
a. Quản trị Truy cập & Bảo mật
- Đăng ký, đăng nhập tài khoản người dùng.
- Xác thực lớp bảo mật thứ cấp qua mã PIN cục bộ.
b. Quản lý Thu / Chi & Ngân sách
- Ghi nhận giao dịch Thu/Chi phân theo từng danh mục (Ăn uống, Cà phê, Xăng xe, Mua sắm...).
- Thiết lập hạn mức Ngân sách (Budget) theo tháng/danh mục.
- Cảnh báo thông minh: Hệ thống tự động kiểm tra và đưa ra cảnh báo (màu đỏ) khi chi tiêu thực tế vượt quá định mức ngân sách.
c. Quản lý Tín dụng & Tính toán Khoản vay (Module chính đảm nhận)
- Ghi nhận các khoản đi vay / cho vay cá nhân, hộ kinh doanh hoặc doanh nghiệp.
- Thuật toán tính lãi: Hỗ trợ 2 phương thức tính lãi phổ biến:
  + Lãi phẳng (Trả đều hàng tháng).
  + Lãi giảm dần theo dư nợ thực tế.
- Tự động lập lịch trình trả nợ: Xuất danh sách chi tiết số tiền gốc, tiền lãi và tổng số tiền phải trả cho từng kỳ.
- Tự động liên kết Ngân sách: Tự động khởi tạo một mục Ngân sách dự phòng trả nợ trong module Chi tiêu khi tạo mới khoản vay.
d. Thống kê & Báo cáo Trực quan
- Dashboard tổng quan: Hiển thị tổng số dư, tổng thu/chi trong tháng.
- Trực quan hóa dữ liệu bằng Donut Chart (tỷ lệ phân bổ chi tiêu) và Bar Chart (so sánh biến động giữa các tháng).

3. Phân tích & Thiết kế Hệ thống (System Analysis & Architecture)

Sơ đồ Use Case tổng quát
https://github.com/quangtung10032005-tt/personal-loan-app-ba/blob/main/diagrams/use-case.png

Biểu đồ Thực thể Mối quan hệ (ERD) & Cấu trúc Dữ liệu
https://github.com/quangtung10032005-tt/personal-loan-app-ba/blob/main/diagrams/erd.png
- Hệ thống sử dụng cơ sở dữ liệu cục bộ SQLite/Room với các bảng chính:
 + User: Lưu thông tin tài khoản và mã PIN bảo mật.
 + Category: Danh mục phân loại giao dịch.
 + Transaction/ expenses: Lưu thông tin các khoản thu/chi.
 + Budget: Lưu hạn mức chi tiêu theo tháng.
 + Loan & InstallmentPayment: Lưu thông tin khoản vay và lịch trình thanh toán trả góp.

4. Thiết kế Giao diện (UI/UX Wireframes)
| Màn hình Vay vốn | Màn hình Kế hoạch Ngân sách | Màn hình Báo cáo Thống kê |

| https://github.com/quangtung10032005-tt/personal-loan-app-ba/blob/main/mockups/screen_loan.png | https://github.com/quangtung10032005-tt/personal-loan-app-ba/blob/main/mockups/screen_budget.png | https://github.com/quangtung10032005-tt/personal-loan-app-ba/blob/main/mockups/screen_report.png |

5. Công nghệ & Công cụ sử dụng
- Phân tích & Mô hình hóa: BPMN, UML (Use Case, ERD Diagram), Draw.io / PlantUML.
- Nền tảng phát triển: Android SDK (Java), Material Design Components.
- Cơ sở dữ liệu: SQLite / Room Database.
- Truy vấn & Kiểm thử: SQL, Postman.

6. Tài liệu đính kèm (Documentation)
[Tài liệu Báo cáo Đặc tả Hệ thống (PDF)](/docs/2_BaoCaoBTL_Android_UDVayVon&ChiTieu_v1.pdf)
