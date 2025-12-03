package org.example.backend.foodpick.domain.alarm.model;

public enum AlarmType {
    REPORT_RECEIVED,        // 신고가 접수됨 (관리자용)
    INQUIRY_RECEIVED,       // 문의 접수됨 (관리자용)
    REVIEW_LIKE,            // 누가 내 리뷰에 좋아요
    REVIEW_COMMENT,         // 누가 내 리뷰에 댓글
    INQUIRY_ANSWER,         // / 내 문의글에 답변
    WARNING_ADDED,          // 경고 횟수 추가될 때
    SYSTEM                  // 시스템 공지 또는 기타
}
