package app.shosetsu.android.datasource.remote.impl.update

import app.shosetsu.android.datasource.remote.base.IRemoteAppUpdateDataSource
import app.shosetsu.android.domain.model.local.AppUpdateEntity

class UpToDownAppUpdateDataSource : IRemoteAppUpdateDataSource,
	IRemoteAppUpdateDataSource.Downloadable {
	override suspend fun loadAppUpdate(): AppUpdateEntity {
		TODO("Add up-to-down update source")
	}

	override suspend fun downloadAppUpdate(update: AppUpdateEntity): ByteArray {
		TODO("Add up-to-down update source")
	}
}