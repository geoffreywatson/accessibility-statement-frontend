/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package unit.views

import java.util.{Calendar, GregorianCalendar}
import org.jsoup.Jsoup
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.accessibilitystatementfrontend.config.{AppConfig, SourceConfig}
import uk.gov.hmrc.accessibilitystatementfrontend.models.{AccessibilityStatement, Android, Draft, FullCompliance, Ios, Milestone, NoCompliance, PartialCompliance, Visibility}
import uk.gov.hmrc.accessibilitystatementfrontend.parsers.VisibilityParser
import uk.gov.hmrc.accessibilitystatementfrontend.views.html.StatementPage
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class StatementPageSpec extends AnyWordSpec with Matchers with GuiceOneAppPerSuite {
  "Given any Accessibility Statement for a service, rendering a Statement Page" should {
    "return HTML containing the header containing the service name" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<h1 class="govuk-heading-xl">Accessibility statement for fully accessible service name service</h1>"""
      )
    }

    "return the correct title and heading for a mobile app" in new FullSetup {
      val content = Jsoup.parse(fullyAccessibleAndroidStatementHtml)

      val title = content.select("title")
      title.size       shouldBe 1
      title.first.text shouldBe "Accessibility statement for the HMRC Android app – GOV.UK"
      val heading = content.select("h1").first.text
      heading should be(
        """Accessibility statement for the HMRC Android app"""
      )
    }

    "return the correct this page is for text for a mobile app" in new FullSetup {
      fullyAccessibleAndroidStatementHtml should include(
        """This page only contains information about the HMRC Android app"""
      )
    }

    "return HTML containing the correct TITLE element" in new FullSetup {
      val content = Jsoup.parse(fullyAccessibleStatementHtml)

      val title = content.select("title")
      title.size       shouldBe 1
      title.first.text shouldBe "Accessibility statement for fully accessible service name service – GOV.UK"
    }

    "return HTML containing the expected introduction with service URL in the body" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">This page only contains information about the fully accessible service name service, available at https://www.tax.service.gov.uk/fully-accessible."""
      )
    }

    "return HTML containing the expected using service information with service description" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">Fully accessible description.</p>"""
      )
    }

    "return HTML containing the default contact information" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<a class="govuk-link" href="https://www.gov.uk/get-help-hmrc-extra-support">contact HMRC for extra support</a>"""
      )
    }

    "return HTML containing report a problem information with a contact link" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<a class="govuk-link" href="https://www.tax.service.gov.uk/contact/accessibility?service=fas">report the accessibility problem</a>."""
      )
    }

    "return HTML containing report a problem information with a contact link and referrer URL" in new FullSetup {
      val statementPageHtml = statementPage(
        fullyAccessibleServiceStatement,
        referrerUrl = Some("came-from-here"),
        isWelshTranslationAvailable = false
      ).body

      statementPageHtml should include(
        """<a class="govuk-link" href="https://www.tax.service.gov.uk/contact/accessibility?service=fas&amp;referrerUrl=came-from-here">report the accessibility problem</a>"""
      )
    }

    "return HTML containing the correctly formatted dates of when the service was tested" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">The service was last tested on 28 February 2020 and was checked for compliance with WCAG 2.1 AA.</p>"""
      )
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">This page was prepared on 15 March 2020. It was last updated on 1 May 2020.</p>"""
      )
    }

    "not include a list item of accessibility problems if accessibility problems is empty" in new FullSetup {
      fullyAccessibleStatementHtml should not include """<ul class="govuk-list govuk-list--bullet" id="accessibility-problems">"""
    }
  }

  "Given any Welsh Accessibility Statement for a service, rendering a Statement Page" should {
    "return HTML containing the header containing the service name in Welsh" in new WelshSetup {
      fullyAccessibleWelshStatementHtml should include(
        """<h1 class="govuk-heading-xl">Datganiad hygyrchedd ar gyfer fully accessible service name</h1>"""
      )
    }

    "return HTML containing the correct TITLE element in Welsh" in new WelshSetup {
      val content = Jsoup.parse(fullyAccessibleWelshStatementHtml)

      val title = content.select("title")
      title.size       shouldBe 1
      title.first.text shouldBe "Datganiad hygyrchedd ar gyfer fully accessible service name – GOV.UK"
    }

    "return HTML containing the expected introduction with service URL in the body in Welsh" in new WelshSetup {
      fullyAccessibleWelshStatementHtml should include(
        """Mae’r dudalen hon ond yn cynnwys gwybodaeth am wasanaeth fully accessible service name, sydd ar gael yn https://www.tax.service.gov.uk/fully-accessible."""
      )
    }
  }

  "Given an Accessibility Statement for an iOS app, rendering a Statement Page" should {
    "return HTML containing accessibility information specific to iOS devices" in new FullSetup {
      fullyAccessibleIosStatementHtml should include(
        """get around the app using Voice Control"""
      )
    }
  }

  "Given an Accessibility Statement for an Android app, rendering a Statement Page" should {
    "return HTML containing accessibility information specific to Android devices" in new FullSetup {
      fullyAccessibleAndroidStatementHtml should include(
        """get around the app using Voice Access"""
      )
    }
  }

  "Given an Accessibility Statement for a browser-based service, rendering a Statement Page" should {
    "return HTML containing accessibility information specific to browsers" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """get from the start of the service to the end using speech recognition software"""
      )
    }

    "return HTML relating to what to do if you have difficulty using this service" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """What to do if you have difficulty"""
      )
    }

    "return HTML including the text 'opens in a new tab' for the report problem link" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """report the accessibility problem"""
      )
    }
  }

  "Given an Accessibility Statement for a fully accessible app, rendering a Statement Page" should {
    "return HTML containing the expected accessibility information stating that the app is fully compliant" in new FullSetup {
      fullyAccessibleIosStatementHtml should include(
        """This app is fully compliant with the"""
      )
      fullyAccessibleIosStatementHtml should include(
        """There are no known accessibility issues within this app"""
      )
    }

    "not return HTML relating to what to do if you have difficulty using this service" in new FullSetup {
      fullyAccessibleIosStatementHtml should not include
        """What to do if you have difficulty"""
    }

    "return HTML not including the text 'opens in a new tab' for the report problem link" in new FullSetup {
      fullyAccessibleAndroidStatementHtml should include(
        """report the accessibility problem</a>."""
      )
    }

    "return HTML including iOS specific link" in new FullSetup {
      fullyAccessibleIosStatementHtml should include(
        """href="https://www.apple.com/uk/accessibility/""""
      )

      fullyAccessibleIosStatementHtml should not include
        """href="https://www.android.com/intl/en_uk/accessibility/""""
    }

    "return HTML including Android specific link" in new FullSetup {
      fullyAccessibleAndroidStatementHtml should include(
        """href="https://www.android.com/intl/en_uk/accessibility/""""
      )

      fullyAccessibleAndroidStatementHtml should not include
        """href="https://www.apple.com/uk/accessibility/""""
    }
  }

  "Given an Accessibility Statement for a partially compliant app, rendering a Statement Page" should {
    "return HTML containing the expected accessibility information stating that the app is partially compliant" in new PartialSetup {
      partiallyAccessibleIosStatementHtml should include(
        """This app is partially compliant with the"""
      )
    }
  }

  "Given an Accessibility Statement for a fully accessible service, rendering a Statement Page" should {
    "return HTML containing the expected accessibility information stating that the service is fully compliant" in new FullSetup {
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">This service is fully compliant with the <a class="govuk-link" href="https://www.w3.org/TR/WCAG21/">Web Content Accessibility Guidelines version 2.1 AA standard</a>.</p>"""
      )
      fullyAccessibleStatementHtml should include(
        """<p class="govuk-body">There are no known accessibility issues within this service.</p>"""
      )
    }

    "should not return information on non compliance if milestones are empty" in new FullSetup {
      fullyAccessibleStatementHtml should not include """<h3 class="govuk-heading-m">Non-accessible content</h3>"""
      fullyAccessibleStatementHtml should not include """<p class="govuk-body">The content listed below is non-accessible for the following reasons.</p>"""
      fullyAccessibleStatementHtml should not include """<h4 class="govuk-heading-s">Non-compliance with the accessibility regulations</h4>"""
    }

    "should not return information on non compliance even if milestones are non-empty" in new FullSetup
      with PartialSetup {
      val fullyAccessibleWithMilestonesPage = statementPage(
        fullyAccessibleServiceStatement.copy(
          milestones = partiallyAccessibleServiceStatement.milestones
        ),
        None,
        isWelshTranslationAvailable = false
      ).body

      fullyAccessibleWithMilestonesPage should not include """First milestone to be fixed"""
      fullyAccessibleWithMilestonesPage should not include """This will be fixed by"""
    }

    "should return information on accessibility problems if problems are non-empty" in new PartialSetup with FullSetup {
      val fullyAccessibleWithProblems = fullyAccessibleServiceStatement.copy(
        accessibilityProblems = partiallyAccessibleServiceStatement.accessibilityProblems
      )
      val statementPageHtml           = statementPage(
        fullyAccessibleWithProblems,
        None,
        isWelshTranslationAvailable = false
      ).body

      statementPageHtml should include(
        """<p class="govuk-body">Some people may find parts of this service difficult to use:</p>"""
      )
      statementPageHtml should include(
        """<ul class="govuk-list govuk-list--bullet" id="accessibility-problems">"""
      )
      statementPageHtml should include(
        """<li>This is the first accessibility problem</li>"""
      )
      statementPageHtml should include(
        """<li>And then this is another one</li>"""
      )
    }
  }

  "Given an Accessibility Statement for a partially accessible service, rendering a Statement Page" should {
    "return HTML containing the expected accessibility information stating that the service is partially compliant" in new PartialSetup {
      partiallyAccessibleStatementHtml should include(
        """<p class="govuk-body">This service is partially compliant with the <a class="govuk-link" href="https://www.w3.org/TR/WCAG21/">Web Content Accessibility Guidelines version 2.1 AA standard</a>.</p>"""
      )
    }

    "return HTML containing a list of the known accessibility issues" in new PartialSetup {
      partiallyAccessibleStatementHtml should include(
        """<p class="govuk-body">Some people may find parts of this service difficult to use:</p>"""
      )
      partiallyAccessibleStatementHtml should include(
        """<ul class="govuk-list govuk-list--bullet" id="accessibility-problems">"""
      )
      partiallyAccessibleStatementHtml should include(
        """<li>This is the first accessibility problem</li>"""
      )
      partiallyAccessibleStatementHtml should include(
        """<li>And then this is another one</li>"""
      )
    }

    "return HTML stating that the service has known compliance issues" in new PartialSetup {
      partiallyAccessibleStatementHtml should include(
        """This service is partially compliant with the  <a class="govuk-link" href="https://www.w3.org/TR/WCAG21/">Web Content Accessibility Guidelines version 2.1 AA standard</a>, due to the non-compliances listed below."""
      )
    }

    "return HTML containing a list of non-accessible content, and when it will be fixed" in new PartialSetup {
      partiallyAccessibleStatementHtml should include(
        """<h3 class="govuk-heading-m">Non‐accessible content</h3>"""
      )
      partiallyAccessibleStatementHtml should include(
        """<p class="govuk-body">The content listed below is non-accessible for the following reasons."""
      )
      partiallyAccessibleStatementHtml should include(
        """<h4 class="govuk-heading-s">Non‐compliance with the accessibility regulations</h4>"""
      )

      partiallyAccessibleStatementHtml should include(
        """First milestone to be fixed. This will be fixed by 15 January 2022."""
      )

      partiallyAccessibleStatementHtml should include(
        """Second milestone we&#x27;ll look at. This will be fixed by 20 June 2022."""
      )

      partiallyAccessibleStatementHtml should include(
        """Then we&#x27;ll get to this third milestone. This will be fixed by 2 September 2022."""
      )
    }

    "return HTML containing a language toggle" in new PartialSetup {
      val statementPageHtml =
        statementPage(
          partiallyAccessibleServiceStatement,
          None,
          isWelshTranslationAvailable = true
        ).body

      val content = Jsoup.parse(statementPageHtml)

      val languageSelect = content.select(".hmrc-language-select")
      languageSelect.size shouldBe 1
    }

    "not return HTML containing a language toggle if only English is available" in new PartialSetup {
      val content = Jsoup.parse(partiallyAccessibleStatementHtml)

      val languageSelect = content.select(".hmrc-language-select")
      languageSelect.size shouldBe 0
    }
  }

  "Given an Accessibility Statement for a non compliant service, rendering a Statement Page" should {
    "include a statement that the service is non compliant" in new NonCompliantSetup {
      nonCompliantAccessibleStatementHtml should include(
        """<p class="govuk-body">This service is non compliant with the <a class="govuk-link" href="https://www.w3.org/TR/WCAG21/">Web Content Accessibility Guidelines version 2.1 AA standard</a>. This service has not yet been checked for compliance so some users may find parts of the service difficult to use.</p>"""
      )
    }

    "return HTML which does NOT contain a list  accessibility issues" in new NonCompliantSetup {
      nonCompliantAccessibleStatementHtml should not include """<ul class="govuk-list govuk-list--bullet" id="accessibility-problems">"""
    }

    "return HTML which states that the service has not been tested for accessibility" in new NonCompliantSetup {
      nonCompliantAccessibleStatementHtml should include(
        """<p class="govuk-body">The service has not been tested for compliance with WCAG 2.1 AA.</p>"""
      )
    }

    "should not return information on non compliance even if milestones are non-empty" in new NonCompliantSetup {
      nonCompliantAccessibleStatementHtml should not include """<p class="govuk-body">First milestone to be fixed</p>"""
      nonCompliantAccessibleStatementHtml should not include """<p class="govuk-body">This will be fixed by 15 January 2022</p>"""
    }

    "should return HTML with a fixed date for carrying out an assessment" in new NonCompliantSetup {
      nonCompliantAccessibleStatementHtml should include(
        """<p class="govuk-body">It has not been tested for compliance with WCAG 2.1 AA. The service will book a full accessibility audit by 31 March 2022.</p>"""
      )
    }
  }

  "Given an accessibility statement that is partially compliant, where only automated testing has been carried out, " +
    "rendering a Statement Page"                                                             should {
      "return HTML with information that the testing was automated" in new PartialSetup {
        automatedTestingStatementPage should include(
          """<p class="govuk-body">The service was last tested on 21 April 2019 using automated tools and was checked for compliance with WCAG 2.1 AA.</p>"""
        )
      }

      "return HTML with the date for carrying out a full assessment" in new PartialSetup {
        automatedTestingStatementPage should include(
          """ <p class="govuk-body">The service will also book a full accessibility audit by 31 March 2022.</p>"""
        )
      }

      "return HTML with the description of the automated tools used" in new PartialSetup {
        automatedTestingStatementPage should include(
          """ <p class="govuk-body">The content listed below is non-accessible for the following reasons. This service was tested using automated tools only.</p>"""
        )
      }
    }

  trait Setup {
    val statementPage = app.injector.instanceOf[StatementPage]

    implicit val fakeRequest: FakeRequest[_] = FakeRequest()
    val configuration                        = Configuration.from(
      Map("platform.frontend.host" -> "https://www.tax.service.gov.uk")
    )

    implicit val sourceConfig: SourceConfig         =
      app.injector.instanceOf[SourceConfig]
    implicit val servicesConfig: ServicesConfig     =
      app.injector.instanceOf[ServicesConfig]
    implicit val visibilityParser: VisibilityParser =
      app.injector.instanceOf[VisibilityParser]

    implicit val appConfig: AppConfig =
      AppConfig(configuration, servicesConfig, visibilityParser)

    val messagesApi: MessagesApi    = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(fakeRequest)

  }

  trait FullSetup extends Setup {
    lazy val fullyAccessibleServiceStatement = AccessibilityStatement(
      serviceName = "fully accessible service name",
      serviceDescription = "Fully accessible description.",
      serviceDomain = "www.tax.service.gov.uk",
      serviceUrl = "/fully-accessible",
      mobilePlatform = None,
      contactFrontendServiceId = "fas",
      complianceStatus = FullCompliance,
      accessibilityProblems = None,
      milestones = None,
      automatedTestingOnly = None,
      statementVisibility = Draft,
      serviceLastTestedDate = Some(new GregorianCalendar(2020, Calendar.FEBRUARY, 28).getTime),
      statementCreatedDate = new GregorianCalendar(2020, Calendar.MARCH, 15).getTime,
      statementLastUpdatedDate = new GregorianCalendar(2020, Calendar.MAY, 1).getTime,
      automatedTestingDetails = None
    )

    lazy val fullyAccessibleIosAppStatement = fullyAccessibleServiceStatement.copy(
      serviceName = "HMRC iOS app",
      mobilePlatform = Some(Ios)
    )

    lazy val fullyAccessibleAndroidAppStatement = fullyAccessibleServiceStatement.copy(
      serviceName = "HMRC Android app",
      mobilePlatform = Some(Android)
    )

    lazy val fullyAccessibleStatementHtml =
      statementPage(
        fullyAccessibleServiceStatement,
        None,
        isWelshTranslationAvailable = false
      ).body

    lazy val fullyAccessibleIosStatementHtml = statementPage(
      fullyAccessibleIosAppStatement,
      None,
      isWelshTranslationAvailable = false
    ).body

    lazy val fullyAccessibleAndroidStatementHtml = statementPage(
      fullyAccessibleAndroidAppStatement,
      None,
      isWelshTranslationAvailable = false
    ).body
  }

  trait WelshSetup extends FullSetup {
    override implicit val messages: Messages = messagesApi.preferred(Seq(Lang("cy")))

    lazy val fullyAccessibleWelshStatementHtml =
      statementPage(
        fullyAccessibleServiceStatement,
        None,
        isWelshTranslationAvailable = false
      ).body
  }

  trait PartialSetup extends Setup {
    lazy val partiallyAccessibleServiceStatement = AccessibilityStatement(
      serviceName = "partially accessible service name",
      serviceDescription = "Partially accessible description.",
      serviceDomain = "www.tax.service.gov.uk",
      serviceUrl = "/partially-accessible",
      mobilePlatform = None,
      contactFrontendServiceId = "pas",
      complianceStatus = PartialCompliance,
      automatedTestingOnly = None,
      accessibilityProblems = Some(
        Seq(
          "This is the first accessibility problem",
          "And then this is another one"
        )
      ),
      milestones = Some(
        Seq(
          Milestone(
            "First milestone to be fixed.",
            new GregorianCalendar(2022, Calendar.JANUARY, 15).getTime
          ),
          Milestone(
            "Second milestone we'll look at.",
            new GregorianCalendar(2022, Calendar.JUNE, 20).getTime
          ),
          Milestone(
            "Then we'll get to this third milestone.",
            new GregorianCalendar(2022, Calendar.SEPTEMBER, 2).getTime
          )
        )
      ),
      statementVisibility = Draft,
      serviceLastTestedDate = Some(new GregorianCalendar(2019, Calendar.APRIL, 21).getTime),
      statementCreatedDate = new GregorianCalendar(2019, Calendar.JUNE, 14).getTime,
      statementLastUpdatedDate = new GregorianCalendar(2019, Calendar.OCTOBER, 7).getTime,
      automatedTestingDetails = None
    )

    lazy val partiallyAccessibleIosAppStatement = partiallyAccessibleServiceStatement.copy(
      mobilePlatform = Some(Ios)
    )

    lazy val partiallyAccessibleIosStatementHtml = statementPage(
      partiallyAccessibleIosAppStatement,
      None,
      isWelshTranslationAvailable = false
    ).body

    lazy val partiallyAccessibleAndroidAppStatement = partiallyAccessibleServiceStatement.copy(
      mobilePlatform = Some(Android)
    )

    lazy val partiallyAccessibleStatementHtml = statementPage(
      partiallyAccessibleServiceStatement,
      None,
      isWelshTranslationAvailable = false
    ).body

    lazy val automatedTestingServiceStatementHtml =
      partiallyAccessibleServiceStatement.copy(
        serviceName = "automated accessible service name",
        serviceDescription = "Automated accessible description.",
        serviceDomain = "www.tax.service.gov.uk",
        serviceUrl = "/automated-accessible",
        contactFrontendServiceId = "aas",
        automatedTestingDetails = Some("This service was tested using automated tools only."),
        automatedTestingOnly = Some(true)
      )

    lazy val automatedTestingStatementPage = statementPage(
      automatedTestingServiceStatementHtml,
      None,
      isWelshTranslationAvailable = false
    ).body
  }

  trait NonCompliantSetup extends PartialSetup {
    lazy val nonCompliantServiceStatement = partiallyAccessibleServiceStatement.copy(
      serviceName = "non accessible service name",
      serviceDescription = "Non accessible description.",
      serviceUrl = "/non-accessible",
      contactFrontendServiceId = "nas",
      complianceStatus = NoCompliance
    )

    lazy val nonCompliantAccessibleStatementHtml = statementPage(
      nonCompliantServiceStatement,
      None,
      isWelshTranslationAvailable = false
    ).body
  }
}
