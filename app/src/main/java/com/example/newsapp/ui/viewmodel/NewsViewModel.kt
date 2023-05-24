package com.example.newsapp.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_ETHERNET
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.data.api.model.Article
import com.example.newsapp.data.api.model.NewsResponse
import com.example.newsapp.data.repo.NewsRepository
import com.example.newsapp.utils.NewsApplication
import com.example.newsapp.utils.Resources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    app: Application, private val repository: NewsRepository
) : AndroidViewModel(app) {
    val breakingNews: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var breakingNewsPage = 1
    private var breakingNewsResponse: NewsResponse? = null

    val searchNewsLiveData: MutableLiveData<Resources<NewsResponse>> = MutableLiveData()
    var searchPage = 1
    private var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch(Dispatchers.IO) {
        safeBreakingCall(countryCode)
    }

    fun searchNews(searchQuery: String) = viewModelScope.launch(Dispatchers.IO) {
        safeSearchCall(searchQuery)
    }

    fun saveArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        repository.upsert(article)
    }

    fun deleteArticle(article: Article) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteArticle(article)
    }

    fun getSaveNews() = repository.getSaveNews()


    private suspend fun safeBreakingCall(countryCode: String) {
        breakingNews.postValue(Resources.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resources.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resources.Error("Network failure"))
                else -> breakingNews.postValue(Resources.Error("Conversion Error"))
            }
        }
    }


    private suspend fun safeSearchCall(searchQuery: String) {
        searchNewsLiveData.postValue(Resources.Loading())
        try {
            if (hasInternetConnection()) {
                val response = repository.searchNews(searchQuery, searchPage)
                searchNewsLiveData.postValue(performSearchNews(response))
            } else {
                searchNewsLiveData.postValue(Resources.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNewsLiveData.postValue(Resources.Error("Network failure"))
                else -> searchNewsLiveData.postValue(Resources.Error("Conversion Error"))
            }
        }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resources<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let { responseResult ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = responseResult
                } else {
                    val oldArticle = breakingNewsResponse?.articles
                    val newArticle = responseResult.articles
                    oldArticle?.addAll(newArticle)
                }
                Resources.Success(breakingNewsResponse ?: responseResult)
            } ?: Resources.Error("Empty response Body")
        } else {
            Resources.Error(response.message())
        }
    }

    private fun performSearchNews(response: Response<NewsResponse>): Resources<NewsResponse> {
        return if (response.isSuccessful) {
            response.body()?.let { responseResult ->
                searchPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = responseResult
                } else {
                    val oldArticle = searchNewsResponse?.articles
                    val newArticle = responseResult.articles
                    oldArticle?.addAll(newArticle)
                }
                Resources.Success(searchNewsResponse ?: responseResult)
            } ?: Resources.Error("Empty response body")
        } else {
            Resources.Error(response.message())
        }
    }


    @SuppressLint("ObsoleteSdkInt")
    private fun hasInternetConnection(): Boolean {
        val connectivityManager =
            getApplication<NewsApplication>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            return capabilities.hasTransport(TRANSPORT_WIFI) || capabilities.hasTransport(
                TRANSPORT_CELLULAR
            ) || capabilities.hasTransport(TRANSPORT_ETHERNET)
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo?.run {
                type == TYPE_WIFI || type == TYPE_MOBILE || type == TYPE_ETHERNET
            } ?: false
        }
    }
}