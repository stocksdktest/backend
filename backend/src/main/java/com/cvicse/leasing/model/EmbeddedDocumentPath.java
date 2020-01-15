package com.cvicse.leasing.model;
/*
   EmbeddedPocumentPath & documentType:JSONObject/JSONArray
 */
public class EmbeddedDocumentPath {
    private String path;
    private EmbeddedDocumentType embeddedDocumentType;
    private DocumentSearchFactor documentSearchFactor = new DocumentSearchFactor();   //filterFactor in this path

    public DocumentSearchFactor getDocumentSearchFactor() {
        return documentSearchFactor;
    }

    public void setDocumentSearchFactor(DocumentSearchFactor documentSearchFactor) {
        this.documentSearchFactor = documentSearchFactor;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setEmbeddedDocumentType(EmbeddedDocumentType embeddedDocumentType) {
        this.embeddedDocumentType = embeddedDocumentType;
    }

    public EmbeddedDocumentType getEmbeddedDocumentType() {
        return embeddedDocumentType;
    }
}
