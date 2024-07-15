package com.khun.reportassistant.services;

import com.khun.reportassistant.models.NecessaryData;

import java.util.List;

public interface MemberService {
    NecessaryData getMemberList();
    boolean addMembers(List<String> members);
    boolean removeMembers(List<String> members);
}
