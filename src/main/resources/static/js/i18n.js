// Internationalization (i18n) for DocShare
const translations = {
    en: {
        // Navigation
        'nav.dashboard': 'Dashboard',
        'nav.documents': 'Documents',
        'nav.logout': 'Logout',
        'nav.hello': 'Hello',
        
        // Dashboard
        'dashboard.title': 'DocShare',
        'dashboard.welcome': 'Welcome to DocShare!',
        'dashboard.subtitle': 'Your collaborative document editing platform.',
        'dashboard.userInfo': 'User Information:',
        'dashboard.username': 'Username',
        'dashboard.email': 'Email',
        'dashboard.fullName': 'Full Name',
        'dashboard.roles': 'Roles',
        'dashboard.accountCreated': 'Account Created',
        'dashboard.quickActions': 'Quick Actions',
        'dashboard.createNew': 'Create New Document',
        'dashboard.myDocs': 'My Documents',
        'dashboard.sharedWithMe': 'Shared With Me',
        
        // Login
        'login.title': 'Login',
        'login.username': 'Username',
        'login.password': 'Password',
        'login.enterUsername': 'Enter username',
        'login.enterPassword': 'Enter password',
        'login.button': 'Login',
        'login.noAccount': "Don't have an account?",
        'login.register': 'Register',
        'login.failed': 'Login failed',
        'login.error': 'An error occurred. Please try again.',
        
        // Register
        'register.title': 'Create Account',
        'register.fullName': 'Full Name',
        'register.username': 'Username',
        'register.email': 'Email',
        'register.password': 'Password',
        'register.enterFullName': 'Enter full name',
        'register.enterUsername': 'Enter username',
        'register.enterEmail': 'Enter email',
        'register.enterPassword': 'Enter password (min 6 characters)',
        'register.button': 'Register',
        'register.hasAccount': 'Already have an account?',
        'register.login': 'Login',
        
        // Documents
        'docs.title': 'My Documents',
        'docs.newDoc': '+ New Document',
        'docs.myDocs': 'My Documents',
        'docs.sharedWithMe': 'Shared With Me',
        'docs.noDocuments': 'No documents found',
        'docs.owner': 'Owner',
        'docs.permission': 'Permission',
        'docs.lastEdited': 'Last edited',
        'docs.share': 'Share',
        'docs.delete': 'Delete',
        'docs.deleteConfirm': 'Are you sure you want to delete this document?',
        'docs.deleteFailed': 'Failed to delete document',
        
        // Editor
        'editor.untitled': 'Untitled Document',
        'editor.startTyping': 'Start typing your document...',
        'editor.save': 'Save',
        'editor.saving': 'Saving...',
        'editor.saved': 'Saved ✓',
        'editor.saveFailed': 'Save failed',
        'editor.readOnly': 'Read-only',
        'editor.makePublic': 'Make this document public',
        'editor.words': 'words',
        'editor.characters': 'characters',
        
        // Share
        'share.title': 'Share Document',
        'share.addPeople': 'Add People',
        'share.enterUsername': 'Enter username',
        'share.view': 'View',
        'share.edit': 'Edit',
        'share.shareButton': 'Share',
        'share.peopleWithAccess': 'People with access',
        'share.noAccess': 'No one else has access yet',
        'share.remove': 'Remove',
        'share.removeConfirm': 'Remove access for this user?',
        'share.enterUsernameError': 'Please enter a username',
        'share.failed': 'Failed to share document',
        'share.error': 'An error occurred',
        
        // Common
        'common.owner': 'Owner',
        'common.permission': 'Permission',
        
        // Collaboration
        'collab.joined': 'joined the document',
        'collab.left': 'left the document'
    },
    vi: {
        // Navigation
        'nav.dashboard': 'Trang chủ',
        'nav.documents': 'Tài liệu',
        'nav.logout': 'Đăng xuất',
        'nav.hello': 'Xin chào',
        
        // Dashboard
        'dashboard.title': 'DocShare',
        'dashboard.welcome': 'Chào mừng đến với DocShare!',
        'dashboard.subtitle': 'Nền tảng soạn thảo tài liệu cộng tác của bạn.',
        'dashboard.userInfo': 'Thông tin người dùng:',
        'dashboard.username': 'Tên đăng nhập',
        'dashboard.email': 'Email',
        'dashboard.fullName': 'Họ và tên',
        'dashboard.roles': 'Vai trò',
        'dashboard.accountCreated': 'Tài khoản tạo lúc',
        'dashboard.quickActions': 'Thao tác nhanh',
        'dashboard.createNew': 'Tạo tài liệu mới',
        'dashboard.myDocs': 'Tài liệu của tôi',
        'dashboard.sharedWithMe': 'Được chia sẻ với tôi',
        
        // Login
        'login.title': 'Đăng nhập',
        'login.username': 'Tên đăng nhập',
        'login.password': 'Mật khẩu',
        'login.enterUsername': 'Nhập tên đăng nhập',
        'login.enterPassword': 'Nhập mật khẩu',
        'login.button': 'Đăng nhập',
        'login.noAccount': 'Chưa có tài khoản?',
        'login.register': 'Đăng ký',
        'login.failed': 'Đăng nhập thất bại',
        'login.error': 'Đã xảy ra lỗi. Vui lòng thử lại.',
        
        // Register
        'register.title': 'Tạo tài khoản',
        'register.fullName': 'Họ và tên',
        'register.username': 'Tên đăng nhập',
        'register.email': 'Email',
        'register.password': 'Mật khẩu',
        'register.enterFullName': 'Nhập họ và tên',
        'register.enterUsername': 'Nhập tên đăng nhập',
        'register.enterEmail': 'Nhập email',
        'register.enterPassword': 'Nhập mật khẩu (tối thiểu 6 ký tự)',
        'register.button': 'Đăng ký',
        'register.hasAccount': 'Đã có tài khoản?',
        'register.login': 'Đăng nhập',
        
        // Documents
        'docs.title': 'Tài liệu của tôi',
        'docs.newDoc': '+ Tài liệu mới',
        'docs.myDocs': 'Tài liệu của tôi',
        'docs.sharedWithMe': 'Được chia sẻ với tôi',
        'docs.noDocuments': 'Không tìm thấy tài liệu',
        'docs.owner': 'Chủ sở hữu',
        'docs.permission': 'Quyền',
        'docs.lastEdited': 'Chỉnh sửa lần cuối',
        'docs.share': 'Chia sẻ',
        'docs.delete': 'Xóa',
        'docs.deleteConfirm': 'Bạn có chắc muốn xóa tài liệu này?',
        'docs.deleteFailed': 'Xóa tài liệu thất bại',
        
        // Editor
        'editor.untitled': 'Tài liệu chưa đặt tên',
        'editor.startTyping': 'Bắt đầu nhập nội dung...',
        'editor.save': 'Lưu',
        'editor.saving': 'Đang lưu...',
        'editor.saved': 'Đã lưu ✓',
        'editor.saveFailed': 'Lưu thất bại',
        'editor.readOnly': 'Chỉ đọc',
        'editor.makePublic': 'Công khai tài liệu này',
        'editor.words': 'từ',
        'editor.characters': 'ký tự',
        
        // Share
        'share.title': 'Chia sẻ tài liệu',
        'share.addPeople': 'Thêm người dùng',
        'share.enterUsername': 'Nhập tên đăng nhập',
        'share.view': 'Xem',
        'share.edit': 'Chỉnh sửa',
        'share.shareButton': 'Chia sẻ',
        'share.peopleWithAccess': 'Người có quyền truy cập',
        'share.noAccess': 'Chưa có ai được chia sẻ',
        'share.remove': 'Xóa',
        'share.removeConfirm': 'Xóa quyền truy cập của người dùng này?',
        'share.enterUsernameError': 'Vui lòng nhập tên đăng nhập',
        'share.failed': 'Chia sẻ tài liệu thất bại',
        'share.error': 'Đã xảy ra lỗi',
        
        // Common
        'common.owner': 'Chủ sở hữu',
        'common.permission': 'Quyền',
        
        // Collaboration
        'collab.joined': 'đã tham gia tài liệu',
        'collab.left': 'đã rời khỏi tài liệu'
    }
};

// Get current language from localStorage or default to 'en'
function getCurrentLanguage() {
    return localStorage.getItem('language') || 'en';
}

// Set language
function setLanguage(lang) {
    localStorage.setItem('language', lang);
    translatePage();
}

// Translate page
function translatePage() {
    const lang = getCurrentLanguage();
    const elements = document.querySelectorAll('[data-i18n]');
    
    elements.forEach(element => {
        const key = element.getAttribute('data-i18n');
        const translation = translations[lang][key];
        
        if (translation) {
            if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA') {
                element.placeholder = translation;
            } else {
                element.textContent = translation;
            }
        }
    });
    
    // Update language toggle button
    const langToggle = document.getElementById('lang-toggle');
    if (langToggle) {
        langToggle.textContent = lang === 'en' ? '🇻🇳 VI' : '🇬🇧 EN';
    }
}

// Toggle language
function toggleLanguage() {
    const currentLang = getCurrentLanguage();
    const newLang = currentLang === 'en' ? 'vi' : 'en';
    setLanguage(newLang);
}

// Get translation
function t(key) {
    const lang = getCurrentLanguage();
    return translations[lang][key] || key;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    translatePage();
});
