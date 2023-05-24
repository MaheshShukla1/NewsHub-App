package com.example.newsapp.data.repo

import com.example.newsapp.data.api.service.RetrofitInstance
import com.example.newsapp.data.api.model.Article
import com.example.newsapp.data.api.model.NewsResponse
import com.example.newsapp.data.db.ArticleDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class NewsRepository(val db: ArticleDatabase) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int): Response<NewsResponse> =
        withContext(Dispatchers.IO) {
            RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)
        }

    suspend fun searchNews(searchQueries: String, pageNumber: Int): Response<NewsResponse> =
        withContext(Dispatchers.IO) {
            RetrofitInstance.api.searchForNews(searchQueries, pageNumber)
        }

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)


    fun getSaveNews() = db.getArticleDao().getAllArticles()


    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}




