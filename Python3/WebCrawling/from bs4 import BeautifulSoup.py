from bs4 import BeautifulSoup
import requests
import pandas as pd
from tqdm.auto import tqdm
import lxml
from wordcloud import WordCloud
from konlpy.tag import Twitter
from collections import Counter
import matplotlib.pyplot as plt

#데이터 프레임 초기화
data = pd.DataFrame(columns=['title', 'url', 'writer'])

BASE_URL = 'http://boannews.com/media/t_list.asp?Page={}&kind='
MAX_PAGES = 10

for page_num in tqdm(range(1, MAX_PAGES + 1), desc="페이지 크롤링"):
    URL = BASE_URL.format(page_num)

    try:
        site = requests.get(URL)
        site.raise_for_status()

        site_soup = BeautifulSoup(site.text, 'lxml')
        news_list = site_soup.select('div.news_list')

        if not news_list:
            print(f"{page_num}번 페이지. 크롤링할 뉴스 없음. 크롤링 중단!")
            break

        for news in news_list:
            try:
                news_title = news.select_one('span.news_txt').text.strip()
                news_url_suffix = news.find('a').get('href')
                news_url = 'http://boannews.com' + news_url_suffix
                news_writer = news.select_one('span.news_writer').text.strip()

                new_data = [news_title, news_url, news_writer]
                data.loc[len(data)] = new_data
            except AttributeError as e:
                print(f"{page_num}번 페이지 건너뜀. : {e}")
                continue

    except requests.exceptions.RequestException as e:
        print(f"Error accessing page {page_num}: {e}")
        continue

print(f"\n{len(data)} 크롤링 완료.")
data.to_csv('boannews_multi_page.csv', encoding='utf-8-sig', index=False)

# ==================== 워드클라우드 ====================

# CSV 파일 읽기 (이미 data 변수에 있지만, 별도로 읽고 싶다면)
# data = pd.read_csv('boannews_multi_page.csv', encoding='utf-8-sig')

# 모든 제목을 하나의 텍스트로 결합
all_titles = ' '.join(data['title'].tolist())

# Twitter(Okt) 형태소 분석기로 명사 추출
twitter = Twitter()
nouns = twitter.nouns(all_titles)

# 한 글자 단어 제거 및 빈도수 계산
filtered_nouns = [word for word in nouns if len(word) > 1]
word_counts = Counter(filtered_nouns)

# 워드클라우드 생성
wordcloud = WordCloud(
    font_path='C:/Windows/Fonts/malgun.ttf',  # 한글 폰트 경로
    width=800,
    height=600,
    background_color='white',
    max_words=100,
    relative_scaling=0.3,
    colormap='viridis'
).generate_from_frequencies(word_counts)

# 워드클라우드 이미지 저장 및 표시
plt.figure(figsize=(12, 8))
plt.imshow(wordcloud, interpolation='bilinear')
plt.axis('off')
plt.title('보안뉴스 기사 제목 워드클라우드', fontsize=20, pad=20)
plt.tight_layout()
plt.savefig('wordcloud_boannews.png', dpi=300, bbox_inches='tight')
plt.show()

print("\n워드클라우드 이미지가 'wordcloud_boannews.png'로 저장되었습니다.")

# 7. (선택사항) 상위 빈출 단어 출력
print("\n가장 많이 등장한 단어 TOP 20:")
for word, count in word_counts.most_common(20):
    print(f"{word}: {count}회")