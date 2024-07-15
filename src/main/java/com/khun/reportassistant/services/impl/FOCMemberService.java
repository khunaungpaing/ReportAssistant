package com.khun.reportassistant.services.impl;

import com.khun.reportassistant.models.NecessaryData;
import com.khun.reportassistant.services.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class FOCMemberService implements MemberService {
    private final String FULLPATH = "/static/reports/foc/data.yml";
    private final Yaml yaml;

    @Override
    public NecessaryData getMemberList() {
        NecessaryData necessaryData = new NecessaryData();
        try (InputStream inputStream = getClass().getResourceAsStream(FULLPATH)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("file not found!");
            } else {
                Map<String, List<String>> obj = yaml.load(inputStream);
                necessaryData.setFocMember(obj.get("foc_members"));
                necessaryData.setRemoveMember(obj.get("remove_members"));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return necessaryData;
    }

    @Override
    public boolean addMembers(List<String> members) {
        var necessaryData = getMemberList();
        boolean modified = necessaryData.getFocMember().addAll(members);
        if (modified) {
            saveMembers(necessaryData);
        }
        return modified;
    }

    @Override
    public boolean removeMembers(List<String> members) {
        var necessaryData = getMemberList();
        boolean modified = necessaryData.getFocMember().removeAll(members);
        if (modified) {
            saveMembers(necessaryData);
        }
        return modified;
    }

    private void saveMembers(NecessaryData necessaryData) {
        Map<String, List<String>> data = new HashMap<>();
        data.put("foc_members", necessaryData.getFocMember());
        data.put("remove_members", necessaryData.getRemoveMember());
        try (OutputStream outputStream = new FileOutputStream(Objects.requireNonNull(getClass().getResource(FULLPATH)).getFile())) {
            yaml.dump(data, new OutputStreamWriter(outputStream));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
