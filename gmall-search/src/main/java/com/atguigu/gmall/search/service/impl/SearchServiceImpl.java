package com.atguigu.gmall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.pmsinterface.entity.AttrEntity;
import com.atguigu.gmall.pmsinterface.entity.BrandEntity;
import com.atguigu.gmall.pmsinterface.entity.CategoryEntity;
import com.atguigu.gmall.search.entity.Goods;
import com.atguigu.gmall.search.entity.SearchParam;
import com.atguigu.gmall.search.entity.SearchResponseVo;
import com.atguigu.gmall.search.entity.SearchResposneAttrVo;
import com.atguigu.gmall.search.service.SearchService;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParam searchParam) throws IOException {

        SearchResponse searchResponse = restHighLevelClient.search(new SearchRequest(new String[]{"goods"}, bulidDSL(searchParam)), RequestOptions.DEFAULT);

        SearchResponseVo  searchResponseVo = parseResult(searchResponse);
        searchResponseVo.setPageNum(searchParam.getPageNum());
        searchResponseVo.setPageSize(searchParam.getPageSize());

        return searchResponseVo;
    }

    private SearchResponseVo parseResult(SearchResponse searchResponse) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();

        SearchHits outHits = searchResponse.getHits();
        searchResponseVo.setTotal(outHits.getTotalHits());

        SearchHit[] hits = outHits.getHits();
        List<Goods> goodsList = Arrays.stream(hits).map(hit -> {
            String sourceAsString = hit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Text highlightTitle = title.getFragments()[0];
            goods.setTitle(highlightTitle.toString());
            return goods;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");

        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(buckets)){
            List<BrandEntity> brandList = buckets.stream().map(bucket -> {
                BrandEntity brand = new BrandEntity();

                Long key = bucket.getKeyAsNumber().longValue();
                brand.setId(key);

                ParsedStringTerms brandNameAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("brandNameAgg");
                String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
                brand.setName(brandName);

                ParsedStringTerms logoAgg = (ParsedStringTerms) bucket.getAggregations().asMap().get("logoAgg");
                String logo = logoAgg.getBuckets().get(0).getKeyAsString();
                brand.setLogo(logo);

                return brand;
            }).collect(Collectors.toList());

            searchResponseVo.setBrand(brandList);
        }

        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");

        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrBuckets = attrIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(attrBuckets)){
            List<SearchResposneAttrVo> attrList = attrBuckets.stream().map(attr -> {
                SearchResposneAttrVo attrVo = new SearchResposneAttrVo();

                Integer key = attr.getKeyAsNumber().intValue();
                attrVo.setAttrId(key);

                ParsedTerms attrNameAgg = (ParsedStringTerms) attr.getAggregations().asMap().get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                attrVo.setAttrName(attrName);

                ParsedTerms attrValueAgg = (ParsedStringTerms) attr.getAggregations().asMap().get("attrValueAgg");
                List<? extends Terms.Bucket> attrValues = attrValueAgg.getBuckets();
                List<String> attrValueList = attrValues.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

                attrVo.setAttrValue(attrValueList);

                return attrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setFilter(attrList);
        }

        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(categoryBuckets)){
            List<CategoryEntity> categoryList = categoryBuckets.stream().map(c -> {
                CategoryEntity categoryEntity = new CategoryEntity();

                Long key = c.getKeyAsNumber().longValue();
                categoryEntity.setId(key);

                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) c.getAggregations().get("categoryNameAgg");
                String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
                categoryEntity.setName(categoryName);

                return categoryEntity;
            }).collect(Collectors.toList());

            searchResponseVo.setCategory(categoryList);
        }

        return searchResponseVo;
    }

    private SearchSourceBuilder bulidDSL(SearchParam searchParam) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        String keyword = searchParam.getKeyword();
        if(StringUtils.isBlank(keyword)){
            return sourceBuilder;
        }

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        sourceBuilder.query(boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND)));

        if(!CollectionUtils.isEmpty(searchParam.getBrandId())){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }

        if(searchParam.getCategoryId() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", searchParam.getCategoryId()));
        }

        List<String> props = searchParam.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.stream().forEach(System.out::println);
            props.forEach(prop -> {
                String[] attr = StringUtils.split(prop,":");
                Arrays.stream(attr).forEach(System.out::println);
                if(attr[0]!=null && attr.length == 2){
                    String attrId = attr[0];
                    String[] attValues = StringUtils.split(attr[1], "-");
                    BoolQueryBuilder attrBoolQuery = QueryBuilders.boolQuery();
                    attrBoolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrId", attrId));
                    attrBoolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs",attrBoolQuery, ScoreMode.None));
                }
            });

        }

        Integer priceFrom = searchParam.getPriceFrom();
        Integer priceTo = searchParam.getPriceTo();
        if(priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if(priceFrom != null) rangeQuery.gte(priceFrom);
            if(priceTo != null) rangeQuery.lte(priceTo);
            boolQueryBuilder.filter(rangeQuery);
        }

        Boolean store = searchParam.getStore();
        if (store != null) boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));

        Integer sort = searchParam.getSort();
        if(sort != null){
            String field = "_score";
            SortOrder sortOrder = SortOrder.DESC;
            //默认得分排序，1-价格降序 2-价格升序 3-销量降序 4-新品降序
            switch (sort){
                case 1: field = "price";  break;
                case 2: field = "price"; sortOrder = SortOrder.ASC; break;
                case 3: field = "sales"; break;
                case 4: field = "createTime"; break;
            };
            sourceBuilder.sort(field, sortOrder);
        }

        Integer pageNum = searchParam.getPageNum();
        Integer pageSize = searchParam.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        sourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font color='red'>").postTags("</font>"));

        sourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                                    .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                                    .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));

        sourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                                                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        sourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","searchAttrs")
                                            .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                                    .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                                    .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));

        sourceBuilder.fetchSource(new String[]{"skuId","price","subTitle","title","defaultIamge"},null);
        System.out.println(sourceBuilder.toString());
        return  sourceBuilder;
    }


}
