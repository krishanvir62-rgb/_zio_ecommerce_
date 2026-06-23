package com.example.zio_ecommercd.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsOfServiceScreen(
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.material3.MaterialTheme.colorScheme.secondary
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary
                    )
                }
                Text(
                    text = "Terms of Service",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary
                )
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Last Updated: June 22, 2026",
                fontSize = 13.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            SectionText(
                title = "1. Acceptance of Terms",
                body = "By using Zio Store, you agree to these Terms of Service. If you do not agree, please do not use our platform. We reserve the right to update these terms at any time."
            )

            SectionText(
                title = "2. Account Registration",
                body = "You must provide accurate information when creating an account. You are responsible for maintaining the confidentiality of your credentials and for all activities under your account."
            )

            SectionText(
                title = "3. Buying & Selling",
                body = "Sellers are responsible for accurate product descriptions and images. Buyers agree to pay the listed price plus applicable charges. All transactions are processed through our integrated payment system."
            )

            SectionText(
                title = "4. Prohibited Items",
                body = "Listings for prohibited, counterfeit, or illegal items are strictly forbidden. We reserve the right to remove any listing and suspend accounts that violate this policy."
            )

            SectionText(
                title = "5. Payments & Refunds",
                body = "Payments are processed through secure third-party gateways. Refund requests are handled on a case-by-case basis. Contact us within 7 days of delivery for any issues."
            )

            SectionText(
                title = "6. Limitation of Liability",
                body = "Zio Store is not liable for any indirect or incidental damages arising from the use of our platform. Our maximum liability is limited to the amount paid for the product in question."
            )

            SectionText(
                title = "7. Contact",
                body = "For any questions regarding these terms, reach out to us at ziostore62725@gmail.com."
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionText(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = body,
            fontSize = 14.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp
        )
    }
}


