package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.entity.SearchParam;
import com.atguigu.gmall.search.entity.SearchResponseVo;

import java.io.IOException;

public interface SearchService {
    SearchResponseVo search(SearchParam searchParam) throws IOException;
}
