package com.kmong

import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider

fun getAwsCredentials(profileName: String?): AwsCredentials {
    return if (profileName != null) {
        ProfileCredentialsProvider.create(profileName).resolveCredentials()
    } else {
        DefaultCredentialsProvider.create().resolveCredentials()
    }
}