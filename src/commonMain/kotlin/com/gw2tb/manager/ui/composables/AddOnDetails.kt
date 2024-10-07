package com.gw2tb.manager.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gw2tb.manager.gw2addonmanager.generated.resources.*
import com.gw2tb.manager.gw2addonmanager.generated.resources.Res
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_description_unknown
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_uninstall
import com.gw2tb.manager.gw2addonmanager.generated.resources.addon_vendor_unknown
import com.gw2tb.manager.model.AvailableAddOnUpdate
import com.gw2tb.manager.model.catalog.AddOnListing
import com.gw2tb.manager.model.local.LocalAddOn
import com.gw2tb.manager.services.Job
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddOnDetails(
    addOnListing: AddOnListing?,
    localAddOn: LocalAddOn?,
    installAddOn: () -> Unit,
    uninstallAddOn: () -> Unit,
    updateAddOn: () -> Unit,
    navigateToVendor: (String) -> Unit,
    modifier: Modifier = Modifier,
    availableAddOnUpdate: AvailableAddOnUpdate? = null,
    getJobs: () -> List<Job> = ::emptyList,
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1F, fill = true)
            ) {
                Text(
                    text = addOnListing?.addOnName ?: localAddOn?.name ?: error("No add-on listing or local add-on provided"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1F, fill = true)
                            .alignBy(FirstBaseline),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        if (addOnListing != null) {
                            Text(
                                text = "by ",
                                modifier = Modifier
                                    .alignBy(FirstBaseline)
                            )

                            if (addOnListing.vendorUrl != null) {
                                TextButton(
                                    text = addOnListing.vendorName,
                                    onClick = { navigateToVendor(addOnListing.vendorUrl) },
                                    modifier = Modifier
                                        .alignBy(FirstBaseline)
                                )
                            } else {
                                Text(
                                    text = addOnListing.vendorName,
                                    modifier = Modifier
                                        .alignBy(FirstBaseline)
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(Res.string.addon_vendor_unknown),
                                modifier = Modifier
                                    .alignBy(FirstBaseline)
                            )
                        }
                    }

                    Text(
                        text = addOnListing?.download?.version?.toString() ?: localAddOn?.version?.toString() ?: "Version unavailable",
                        modifier = Modifier
                            .alignBy(FirstBaseline),
                        color = Color.Black.copy(ContentAlpha.medium),
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            val jobs = getJobs()

            var exWidth by remember { mutableStateOf(0) }

            Crossfade(targetState = jobs.isEmpty()) { state ->
                if (state) {
                    Column(
                        modifier = Modifier
                            .onGloballyPositioned { layoutCoordinates -> exWidth = maxOf(exWidth, layoutCoordinates.size.width) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val isUpdateAvailable = availableAddOnUpdate != null

                        EmphasisButton(
                            onClick = { if (isUpdateAvailable) updateAddOn() else installAddOn() },
                            modifier = Modifier
                                .width(91.dp),
                            enabled = (localAddOn == null) || isUpdateAvailable
                        ) {
                            Text(
                                text = when {
                                    localAddOn == null -> stringResource(Res.string.addon_install)
                                    isUpdateAvailable -> stringResource(Res.string.addon_update)
                                    else -> stringResource(Res.string.addon_installed)
                                }
                            )
                        }

                        if (localAddOn != null) {
                            TextButton(
                                text = stringResource(Res.string.addon_uninstall),
                                onClick = uninstallAddOn
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .widthIn(min = exWidth.dp)
                            .padding(4.dp)
                            .size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF8ad3d3)
                    )
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        Box {
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                Text(text = addOnListing?.addOnDescription ?: stringResource(Res.string.addon_description_unknown))

                Spacer(
                    modifier = Modifier
                        .heightIn(min = 8.dp)
                        .weight(1F, fill = true)
                )

                // TODO Add links to sources, etc.
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                style = LocalScrollbarStyle.current.copy(
                    thickness = 6.dp,
                    shape = RectangleShape
                )
            )
        }
    }
}