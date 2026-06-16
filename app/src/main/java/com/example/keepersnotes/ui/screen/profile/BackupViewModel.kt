package com.example.keepersnotes.ui.screen.profile

import androidx.lifecycle.ViewModel
import com.example.keepersnotes.data.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    val backupManager: BackupManager
) : ViewModel()
