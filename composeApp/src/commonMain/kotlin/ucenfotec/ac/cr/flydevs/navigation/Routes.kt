/*
 * Copyright 2025 kevinah95 (Kevin A. Hernández Rostrán)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ucenfotec.ac.cr.flydevs.navigation

import kotlinx.serialization.Serializable

@Serializable object AuthStart

@Serializable object Login

@Serializable object Register

@Serializable object CompleteProfile

@Serializable object Home

@Serializable object LaunchList

@Serializable object Profile

@Serializable data class LaunchDetail(val flightNumber: Int)

@Serializable object CardCatalog

@Serializable object MyCollection

@Serializable data class CardDetail(val cardId: String)