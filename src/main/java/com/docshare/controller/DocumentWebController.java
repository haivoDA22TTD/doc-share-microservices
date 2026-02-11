package com.docshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DocumentWebController {
    
    @GetMapping("/documents")
    public String documentsPage() {
        return "documents";
    }
    
    @GetMapping("/documents/new")
    public String newDocumentPage() {
        return "document-editor";
    }
    
    @GetMapping("/documents/{id}")
    public String editDocumentPage(@PathVariable Long id) {
        return "document-editor";
    }
    
    @GetMapping("/documents/{id}/share")
    public String shareDocumentPage(@PathVariable Long id) {
        return "document-share";
    }
}
