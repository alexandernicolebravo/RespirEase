package com.example.respiratoryhealthapp.screens.educationalcontent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.respiratoryhealthapp.data.model.ContentCategory
import com.example.respiratoryhealthapp.data.model.ContentDifficulty
import com.example.respiratoryhealthapp.data.model.EducationalArticle
import com.example.respiratoryhealthapp.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationalContentScreen(
    navController: NavController,
    viewModel: EducationalContentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Educational Content") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.toggleBookmarkedOnly() }) {
                        Icon(
                            if (uiState.showBookmarkedOnly) Icons.Default.Bookmark
                            else Icons.Default.BookmarkBorder,
                            contentDescription = "Show Bookmarked"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search articles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                singleLine = true
            )

            if (uiState.selectedCategory != null || uiState.selectedDifficulty != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedCategory?.let { category ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setCategory(null) },
                            label = { Text(category.displayName) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
            )
                    }
                    uiState.selectedDifficulty?.let { difficulty ->
                        FilterChip(
                            selected = true,
                            onClick = { viewModel.setDifficulty(null) },
                            label = { Text(difficulty.displayName) },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove") }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                    CircularProgressIndicator()
                }
                } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.error}")
                }
                } else if (uiState.articles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No articles found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
                    items(uiState.articles) { article ->
                        ArticleCard(
                            article = article,
                            onArticleClick = {
                                navController.navigate(NavRoutes.EducationalContentDetail.createRoute(article.id))
                            },
                            onBookmarkClick = { viewModel.toggleBookmark(article.id) }
            )
        }
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                selectedCategory = uiState.selectedCategory,
                selectedDifficulty = uiState.selectedDifficulty,
                onCategorySelected = { viewModel.setCategory(it) },
                onDifficultySelected = { viewModel.setDifficulty(it) },
                onDismiss = { showFilterDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleCard(
    article: EducationalArticle,
    onArticleClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onArticleClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onBookmarkClick) {
                    Icon(
                        if (article.isBookmarked) Icons.Default.Bookmark
                        else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text(article.category.displayName) }
                )
                AssistChip(
                    onClick = { },
                    label = { Text("${article.readingTime} min read") }
                )
                AssistChip(
                    onClick = { },
                    label = { Text(article.difficulty.displayName) }
                )
            }
        }
    }
}

@Composable
private fun FilterDialog(
    selectedCategory: ContentCategory?,
    selectedDifficulty: ContentDifficulty?,
    onCategorySelected: (ContentCategory?) -> Unit,
    onDifficultySelected: (ContentDifficulty?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Articles") },
        text = {
            Column {
                Text("Category", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                ContentCategory.entries.forEach { category ->
                    Row(
             modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = category == selectedCategory,
                            onClick = { onCategorySelected(category) }
                        )
                        Text(category.displayName)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Difficulty", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
                ContentDifficulty.entries.forEach { difficulty ->
                    Row(
                 modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDifficultySelected(difficulty) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = difficulty == selectedDifficulty,
                            onClick = { onDifficultySelected(difficulty) }
                        )
                        Text(difficulty.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
                }
            }
    )
} 