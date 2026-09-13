// Vrednosti su iste kao u EventCategory na serveru, uz nazive za prikaz.
// Stoje na jednom mestu, jer ih koriste i forme i pretraga.
export const EVENT_CATEGORIES = [
  { value: 'MUSIC', label: 'Music' },
  { value: 'TECHNOLOGY', label: 'Technology' },
  { value: 'SPORT', label: 'Sport' },
  { value: 'OUTDOORS', label: 'Outdoors' },
  { value: 'FOOD_AND_DRINK', label: 'Food and drink' },
  { value: 'ART_AND_CULTURE', label: 'Art and culture' },
  { value: 'EDUCATION', label: 'Education' },
  { value: 'GAMES', label: 'Games' },
  { value: 'BUSINESS', label: 'Business' },
  { value: 'OTHER', label: 'Something else' }
];

export function categoryLabel(value: string): string {
  const found = EVENT_CATEGORIES.find((category) => category.value === value);
  return found ? found.label : value;
}
