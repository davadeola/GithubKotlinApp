package com.sriyank.javatokotlindemo.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.sriyank.javatokotlindemo.adapters.DisplayAdapter
import com.sriyank.javatokotlindemo.retrofit.GithubAPIService
import android.os.Bundle
import com.sriyank.javatokotlindemo.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.sriyank.javatokotlindemo.retrofit.RetrofitClient
import androidx.appcompat.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import com.sriyank.javatokotlindemo.models.SearchResponse
import androidx.core.view.GravityCompat
import com.sriyank.javatokotlindemo.app.Constants
import com.sriyank.javatokotlindemo.extensions.setUpRecyclerView

import com.sriyank.javatokotlindemo.extensions.showErrorMessage
import com.sriyank.javatokotlindemo.extensions.toast
import com.sriyank.javatokotlindemo.models.Repository
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_display.*
import kotlinx.android.synthetic.main.header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class DisplayActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var displayAdapter: DisplayAdapter
    private lateinit var browsedRepositories: List<Repository>
    private val apiService: GithubAPIService by lazy {
        RetrofitClient.githubAPIService
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)


        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Showing Browsed Results"

        setAppUserName()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager




        nav_view.setNavigationItemSelectedListener(this)

        val drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        val intent = intent
        if (intent.getIntExtra(Constants.KEY_QUERY_TYPE, -1) == Constants.SEARCH_BY_REPO) {
            val queryRepo = intent.getStringExtra(Constants.KEY_REPO_SEARCH)
            val repoLanguage = intent.getStringExtra(Constants.KEY_LANGUAGE)
            queryRepo?.let { repoLanguage?.let { it1 -> fetchRepositories(it, it1) } }
        } else {
            val githubUser = intent.getStringExtra(Constants.KEY_GITHUB_USER)
            fetchUserRepositories(githubUser)
        }
    }

    private fun fetchUserRepositories(githubUser: String?) {
        apiService.searchRepositoriesByUser(githubUser)
            .enqueue(object : Callback<List<Repository>?> {
                override fun onResponse(
                    call: Call<List<Repository>?>,
                    response: Response<List<Repository>?>
                ) {
                    response.body()?.let {
                        consumeRetrofitResponse(response,it)
                    }

                }

                override fun onFailure(call: Call<List<Repository>?>, t: Throwable) {
                   toast(t.message ?: "Error fetching results")
                }
            })
    }

    private fun fetchRepositories(queryRepo: String, repoLanguage: String) {
        var queryRepo = queryRepo
        val query: MutableMap<String, String?> = HashMap()
        if (repoLanguage.isNotEmpty()) queryRepo += " language:$repoLanguage"

        query["q"] = queryRepo

        apiService.searchRepositories(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {

                response.body()?.items?.let { consumeRetrofitResponse(response, it) }

            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                toast( t.toString())

            }
        })
    }

    private fun consumeRetrofitResponse(response: Response<*>, list: List<Repository>){
        if (response.isSuccessful) {
            Log.i(TAG, "posts loaded from API $response")

            if (list.isNotEmpty())
               displayAdapter = recyclerView.setUpRecyclerView(list)
            else
                toast("No Items Found")
        } else {
            Log.i(TAG, "error $response")
            showErrorMessage( response.errorBody()!!)
        }
    }



    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true

        when (menuItem.itemId) {
            R.id.item_bookmark -> { consumeMenuEvent({showBookmarks()}, "Showing Bookmarks")

            }
            R.id.item_browsed_results -> { consumeMenuEvent({showBrowsedResults()},"Showing Browsed Results" )

            }
        }
        return true
    }

   private inline fun consumeMenuEvent(myFunc: ()-> Unit, title: String){
        myFunc()
       closeDrawer()
        supportActionBar!!.title = title
    }

    private fun showBrowsedResults() {
        displayAdapter.swap(browsedRepositories)
    }

    private fun showBookmarks() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction{realm->
            val bookMarkedList = realm.where(Repository::class.java).findAll()
            displayAdapter.swap(bookMarkedList)
        }

    }

    private fun closeDrawer() {
        drawer_layout!!.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawer_layout!!.isDrawerOpen(GravityCompat.START)) closeDrawer() else {
            super.onBackPressed()

        }
    }

    private fun setAppUserName(){
        val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val personName = sp.getString(Constants.KEY_PERSON_NAME, "User")

        val headerView = nav_view.getHeaderView(0)
        headerView.txvName.text = personName

    }

    companion object {
        private val TAG = DisplayActivity::class.java.simpleName
    }
}