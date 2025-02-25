package pl.gov.coi.pomocua.ads;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.gov.coi.pomocua.ads.myoffers.MyOffersRepository;
import pl.gov.coi.pomocua.ads.translatorservice.TranslatorResponse;
import pl.gov.coi.pomocua.ads.translatorservice.TranslatorService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OffersTranslationUtil {

    private final MyOffersRepository offersRepository;
    private final TranslatorService translatorService;

    @Value("${translator.offers-per-minute}")
    private Byte offersPerMinute;

    @Scheduled(fixedDelay = 60000)
    private void translatePendingOffers() {
        List<BaseOffer> offerList = offersRepository.findOffersToTranslate(PageRequest.of(0, offersPerMinute));
        offerList.forEach(offer -> {
            log.info("Translating offer %s - %s".formatted(offer.id, offer.title));
            translateOffer(offer);
            offersRepository.save(offer);
        });
    }

    public void translateOffer(BaseOffer offer) {
        List<TranslatorResponse> translatorResponseList = translatorService.translate(offer.title, offer.description);
        updateOfferWithTranslations(offer, translatorResponseList);
    }

    // offer.title & offer.description filled already with source values
    private void updateOfferWithTranslations(BaseOffer offer, List<TranslatorResponse> translatorResponseList) {
        if (!CollectionUtils.isEmpty(translatorResponseList) && translatorResponseList.size() == 2) {
            String sourceTitle = offer.title;
            String sourceDescription = offer.description;
            TranslatorResponse titleTranslatorResponse = translatorResponseList.get(0);
            TranslatorResponse descriptionTranslatorResponse = translatorResponseList.get(1);
            Language detectedLanguage = descriptionTranslatorResponse.getDetectedLanguage();
            offer.detectedLanguage = detectedLanguage;
            switch (detectedLanguage) {
                case UA -> {
                    offer.titleUa = sourceTitle;
                    offer.descriptionUa = sourceDescription;
                    offer.title = titleTranslatorResponse.getTranslations().get(Language.PL);
                    offer.description = descriptionTranslatorResponse.getTranslations().get(Language.PL);
                    offer.titleEn = titleTranslatorResponse.getTranslations().get(Language.EN);
                    offer.descriptionEn = descriptionTranslatorResponse.getTranslations().get(Language.EN);
                    offer.titleRu = titleTranslatorResponse.getTranslations().get(Language.RU);
                    offer.descriptionRu = descriptionTranslatorResponse.getTranslations().get(Language.RU);
                }
                case PL -> {
                    offer.titleUa = titleTranslatorResponse.getTranslations().get(Language.UA);
                    offer.descriptionUa = descriptionTranslatorResponse.getTranslations().get(Language.UA);
                    offer.titleEn = titleTranslatorResponse.getTranslations().get(Language.EN);
                    offer.descriptionEn = descriptionTranslatorResponse.getTranslations().get(Language.EN);
                    offer.titleRu = titleTranslatorResponse.getTranslations().get(Language.RU);
                    offer.descriptionRu = descriptionTranslatorResponse.getTranslations().get(Language.RU);
                }
                case EN -> {
                    offer.titleUa = titleTranslatorResponse.getTranslations().get(Language.UA);
                    offer.descriptionUa = descriptionTranslatorResponse.getTranslations().get(Language.UA);
                    offer.title = titleTranslatorResponse.getTranslations().get(Language.PL);
                    offer.description = descriptionTranslatorResponse.getTranslations().get(Language.PL);
                    offer.titleEn = sourceTitle;
                    offer.descriptionEn = sourceDescription;
                    offer.titleRu = titleTranslatorResponse.getTranslations().get(Language.RU);
                    offer.descriptionRu = descriptionTranslatorResponse.getTranslations().get(Language.RU);
                }
                case RU -> {
                    offer.titleUa = titleTranslatorResponse.getTranslations().get(Language.UA);
                    offer.descriptionUa = descriptionTranslatorResponse.getTranslations().get(Language.UA);
                    offer.title = titleTranslatorResponse.getTranslations().get(Language.PL);
                    offer.description = descriptionTranslatorResponse.getTranslations().get(Language.PL);
                    offer.titleEn = titleTranslatorResponse.getTranslations().get(Language.EN);
                    offer.descriptionEn = descriptionTranslatorResponse.getTranslations().get(Language.EN);
                    offer.titleRu = sourceTitle;
                    offer.descriptionRu = sourceDescription;
                }
            }
        } else {
            offer.translationErrorCounter++;
        }
    }
}
