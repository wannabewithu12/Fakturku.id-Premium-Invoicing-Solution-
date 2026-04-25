package com.fakturkuid.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fakturkuid.app.R

@Composable
fun StatusBadge(status: String) {
    val color = when (status.lowercase()) {
        "paid" -> Color(0xFF2DD4BF)
        "unpaid" -> Color(0xFF0085FF)
        "overdue" -> Color(0xFFFB7185)
        else -> Color.Gray
    }
    
    val label = when (status.lowercase()) {
        "paid" -> stringResource(R.string.status_paid)
        "unpaid" -> stringResource(R.string.status_unpaid)
        "overdue" -> stringResource(R.string.status_overdue)
        else -> status.uppercase()
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color,
            fontSize = 9.sp
        )
    }
}

object StatusColor {
    fun get(status: String) = when (status.lowercase()) {
        "paid" -> Color(0xFF2DD4BF)
        "unpaid" -> Color(0xFF0085FF)
        "overdue" -> Color(0xFFFB7185)
        else -> Color.Gray
    }
}
