TÀI LIỆU ĐẶC TẢ PHÂN TÍCH HỆ THỐNG (SYSTEM ANALYSIS SPECIFICATION)
Tên dự án: Ứng dụng Vay vốn và Chi tiêu Cá nhân
Đối tượng sử dụng: Sinh viên, người đi làm, cá nhân/hộ kinh doanh nhỏ lẻ
Vai trò phân tích: Business Analyst (BA)
PHẦN I. KHẢO SÁT & BÀI TOÁN NGHIỆP VỤ

1.1. Bối cảnh & Khảo sát thực tế
- Qua khảo sát thực tế các phương pháp quản lý tài chính cá nhân truyền thống (Sổ tay ghi chép, file Excel thu chi, bảng theo dõi trả góp...), người dùng thường gặp các khó khăn:
 + Mất kiểm soát dòng tiền thu/chi hàng tháng do thiếu công cụ cảnh báo tức thì.
 + Khó tính toán chính xác lịch trình trả nợ gốc/lãi đối với các khoản vay trả góp hoặc vay mượn cá nhân.
 + Hầu hết các ứng dụng trên thị trường tách biệt giữa tính năng Quản lý Thu/Chi và Quản lý Khoản vay.

1.2. Mục tiêu hệ thống
Xây dựng ứng dụng di động số hóa quy trình quản lý tài chính cá nhân theo hướng Local-first, tích hợp đồng thời nghiệp vụ Quản lý Thu/Chi và Quản lý Tín dụng cá nhân.

PHẦN II. YÊU CẦU HỆ THỐNG (SYSTEM REQUIREMENTS)

2.1. Yêu cầu chức năng (Functional Requirements)

2.1.1. Module Quản trị & Bảo mật
- Cho phép người dùng Đăng ký / Đăng nhập tài khoản.
- Thiết lập và xác thực mã PIN bảo mật cục bộ khi mở ứng dụng.

2.1.2. Module Quản lý Thu / Chi & Ngân sách
- Quản lý giao dịch Thu/Chi (CRUD): Thêm, Xem, Sửa, Xóa khoản thu hoặc chi.
- Quản lý Danh mục (Category): Phân loại giao dịch (Ăn uống, Mua sắm, Cà phê, Xăng xe...).
- Lập kế hoạch Ngân sách (Budget): Thiết lập định mức chi tiêu theo tháng hoặc theo từng danh mục.
- Cảnh báo Vượt hạn mức: Tự động so sánh số tiền đã chi với ngân sách và cảnh báo khi Số tiền đã chi > Hạn mức ngân sách.

2.1.3. Module Quản lý Tín dụng & Vay vốn (Module Trọng tâm)
- Tạo mới hồ sơ khoản vay (Mục đích, Số tiền, Thời hạn, Lãi suất, Đối tượng vay).
- Áp dụng công thức tính toán lãi suất:
  + Lãi phẳng (Fixed Rate): Lãi suất tính cố định trên tổng số tiền gốc ban đầu.
  + Dư nợ giảm dần (Reducing Balance): Lãi suất tính trên số dư nợ gốc thực tế còn lại của từng kỳ.
- Lập lịch trình trả nợ chi tiết (Loan Schedule): Xuất danh sách theo từng kỳ bao gồm (Kỳ, Tiền gốc, Tiền lãi, Tổng thanh toán, Dư nợ còn lại).
- Tự động tạo Ngân sách dự phòng trả nợ liên kết sang Module Chi tiêu.

2.1.4. Module Báo cáo & Thống kê
- Dashboard hiển thị tổng số dư, tổng thu, tổng chi trong khoảng thời gian chọn.
- Trực quan hóa dữ liệu bằng Biểu đồ tròn (Tỷ lệ phân bổ chi tiêu) và Biểu đồ cột (So sánh thu chi giữa các tháng).

2.2. Yêu cầu phi chức năng (Non-Functional Requirements)
- Hiệu năng: Phản hồi thao tác CRUD < 1 giây. Việc xử lý vẽ biểu đồ và tính toán lịch trình vay không gây treo giao diện (UI Thread).
- Tính khả dụng - Usability: Thiết kế theo chuẩn Material Design. Thao tác thêm mới giao dịch không quá 4 bước chạm.
- Bảo mật: Mã PIN và thông tin tài khoản được mã hóa trước khi lưu xuống CSDL SQLite.
- Khả năng hoạt động: Hoạt động hoàn toàn Offline dựa trên cơ sở dữ liệu Room/SQLite cục bộ.

PHẦN III. MÔ HÌNH HOÁ DỮ LIỆU & QUY TRÌNH (UML)

3.1. Danh mục Use Cases chính

UC01_DangNhap_DangKy: Quản lý truy cập.
UC02_XacThucPIN: Bảo mật ứng dụng.
UC03_QuanLyGiaoDich: Thêm/Sửa/Xóa khoản thu chi.
UC04_ThietLapNganSach: Đặt định mức chi tiêu.
UC05_TinhKhoanVay: Nhập thông số và tính gốc/lãi khoản vay.
UC06_LapLichTraNo: Tự động tạo danh sách thanh toán từng kỳ.
UC07_XemBaoCaoThongKe: Xem biểu đồ phân tích tài chính.
(Chi tiết sơ đồ Use Case xem tại diagrams/use-case.png và ERD xem tại diagrams/erd.png)
